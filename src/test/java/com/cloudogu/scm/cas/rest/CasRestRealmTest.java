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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

    when(authenticationInfoBuilder.create("ST-123", SERVICE_URL)).thenReturn(authenticationInfo);

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

  private void bindConfiguration(boolean enabled) {
    Configuration configuration = new Configuration();
    configuration.setEnabled(enabled);
    when(context.get()).thenReturn(configuration);
  }
}
