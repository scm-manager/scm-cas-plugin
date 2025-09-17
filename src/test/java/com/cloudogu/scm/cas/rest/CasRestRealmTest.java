/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.scm.cas.rest;

import com.cloudogu.scm.cas.AuthenticationInfoBuilder;
import com.cloudogu.scm.cas.CasContext;
import com.cloudogu.scm.cas.Configuration;
import com.cloudogu.scm.cas.ServiceUrlProvider;
import com.google.inject.util.Providers;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cache.CacheManager;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CasRestRealmTest {

  private static final String SERVICE_URL = "https://scm.hitchhiker.com";

  @Mock
  private CasContext context;

  @Mock
  private AuthenticationInfoBuilder authenticationInfoBuilder;

  @Mock
  private CasRestClient restClient;

  @Mock
  private ServiceUrlProvider serviceUrlProvider;

  @Mock
  private AuthenticationInfo authenticationInfo;

  @Mock
  private InvalidCredentialsCache invalidCredentialsCache;

  @Mock
  private CacheManager cacheManager;

  private CasRestRealm realm;

  @BeforeEach
  void setUpObjectUnderTest() {
    realm = new CasRestRealm(context, authenticationInfoBuilder, Providers.of(restClient), serviceUrlProvider, cacheManager, invalidCredentialsCache);
  }

  @Test
  void shouldReturnAuthenticationInfo() {
    bindConfiguration(true);

    String tgtLocation = "https://cas.hitchhiker/v1/tickets/TGT-123";
    when(restClient.requestGrantingTicketUrl("trillian", "secret")).thenReturn(tgtLocation);

    when(serviceUrlProvider.create()).thenReturn(SERVICE_URL);
    when(restClient.requestServiceTicket(tgtLocation, SERVICE_URL)).thenReturn("ST-123");

    when(authenticationInfoBuilder.create("ST-123", SERVICE_URL, "secret")).thenReturn(authenticationInfo);

    UsernamePasswordToken token = new UsernamePasswordToken("trillian", "secret".toCharArray());
    AuthenticationInfo result = realm.doGetAuthenticationInfo(token);

    assertThat(result).isSameAs(authenticationInfo);
  }

  @Test
  void shouldReturnNullIfCasIsDisabled() {
    bindConfiguration(false);

    UsernamePasswordToken token = new UsernamePasswordToken("trillian", "secret".toCharArray());
    AuthenticationInfo result = realm.doGetAuthenticationInfo(token);

    assertThat(result).isNull();
  }

  @Test
  void shouldNotQueryCasIfInvalidPasswordIsCached() {
    bindConfiguration(true);
    UsernamePasswordToken token = new UsernamePasswordToken("trillian", "secret".toCharArray());
    doThrow(AuthenticationException.class).when(invalidCredentialsCache).verifyNotInvalid(token);

    assertThrows(AuthenticationException.class, () -> realm.getAuthenticationInfo(token));

    verifyNoInteractions(restClient);
  }

  @Test
  void shouldCacheInvalidCredential() {
    bindConfiguration(true);

    when(restClient.requestGrantingTicketUrl("trillian", "secret")).thenThrow(new AuthenticationException());

    UsernamePasswordToken token = new UsernamePasswordToken("trillian", "secret".toCharArray());
    assertThrows(AuthenticationException.class, () -> realm.getAuthenticationInfo(token));

    verify(invalidCredentialsCache).cacheAsInvalid(any());
  }

  @Nested
  class ConcurrentRequests {
    private static final long SIMULATED_DELAY_MS = 100L;

    private final Queue<Long> executionStartTimes = new ConcurrentLinkedQueue<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    private final CountDownLatch startLatch = new CountDownLatch(1);
    private final CountDownLatch finishLatch = new CountDownLatch(2);

    @BeforeEach
    void enable() {
      bindConfiguration(true);

      when(restClient.requestGrantingTicketUrl(anyString(), anyString()))
        .thenAnswer(invocation -> {
          executionStartTimes.add(System.currentTimeMillis());
          Thread.sleep(SIMULATED_DELAY_MS);
          return "GT-for-" + invocation.getArgument(0);
        });
    }

    @Test
    void shouldExecuteSequentiallyForSameUser() throws InterruptedException {
      UsernamePasswordToken token = new UsernamePasswordToken("trillian", "secret");

      submit(executor, token);
      submit(executor, token);

      executeRequests();

      assertThat(executionStartTimes).hasSize(2);

      long difference = getDifference();

      // The difference in start times MUST be >= the simulated delay,
      // proving one task waited for the other.
      assertThat(difference).isGreaterThanOrEqualTo(SIMULATED_DELAY_MS);
    }

    @Test
    void shouldExecuteInParallelForDifferentUsers() throws InterruptedException {
      UsernamePasswordToken tokenArthur = new UsernamePasswordToken("arthur", "secret");
      UsernamePasswordToken tokenFord = new UsernamePasswordToken("ford", "secret");

      submit(executor, tokenArthur);
      submit(executor, tokenFord);

      executeRequests();

      assertThat(executionStartTimes).hasSize(2);

      long difference = getDifference();

      // The difference in start times MUST be < the simulated delay,
      // proving they ran concurrently in parallel.
      assertThat(difference).isLessThan(SIMULATED_DELAY_MS);
    }

    private void submit(ExecutorService executor, UsernamePasswordToken token) {
      executor.submit(() -> {
        try {
          startLatch.await(); // Wait for the signal to start
          realm.doGetAuthenticationInfo(token);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          finishLatch.countDown(); // Signal that this task is finished
        }
      });
    }

    private void executeRequests() throws InterruptedException {
      startLatch.countDown();
      finishLatch.await(2, TimeUnit.SECONDS);
      executor.shutdown();
    }

    private long getDifference() {
      Long t1 = executionStartTimes.poll();
      Long t2 = executionStartTimes.poll();
      long difference = Math.abs(t2 - t1);
      return difference;
    }
  }

  private void bindConfiguration(boolean enabled) {
    Configuration configuration = new Configuration();
    configuration.setEnabled(enabled);
    when(context.get()).thenReturn(configuration);
  }
}
