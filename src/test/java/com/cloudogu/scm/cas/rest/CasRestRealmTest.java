/**
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.cloudogu.scm.cas.rest;

import com.cloudogu.scm.cas.AuthenticationInfoBuilder;
import com.cloudogu.scm.cas.CasContext;
import com.cloudogu.scm.cas.Configuration;
import com.cloudogu.scm.cas.ServiceUrlProvider;
import com.google.inject.util.Providers;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
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

  private CasRestRealm realm;

  @BeforeEach
  void setUpObjectUnderTest() {
    realm = new CasRestRealm(context, authenticationInfoBuilder, Providers.of(restClient), serviceUrlProvider);
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

  private void bindConfiguration(boolean enabled) {
    Configuration configuration = new Configuration();
    configuration.setEnabled(enabled);
    when(context.get()).thenReturn(configuration);
  }
}
