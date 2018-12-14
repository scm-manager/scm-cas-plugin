package com.cloudogu.scm.cas;

import com.google.inject.util.Providers;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CasRestRealmTest {

  private static final String SERVICE_URL = "https://scm.hitchhiker.com";

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
    realm = new CasRestRealm(authenticationInfoBuilder, Providers.of(restClient), serviceUrlProvider);
  }

  @Test
  void shouldReturnAuthenticationInfo() {
    String tgtLocation = "https://cas.hitchhiker/v1/tickets/TGT-123";
    when(restClient.requestGrantingTicketUrl("trillian", "secret")).thenReturn(tgtLocation);

    when(serviceUrlProvider.create()).thenReturn(SERVICE_URL);
    when(restClient.requestServiceTicket(tgtLocation, SERVICE_URL)).thenReturn("ST-123");

    when(authenticationInfoBuilder.create("ST-123", SERVICE_URL)).thenReturn(authenticationInfo);

    UsernamePasswordToken token = new UsernamePasswordToken("trillian", "secret".toCharArray());
    AuthenticationInfo result = realm.doGetAuthenticationInfo(token);

    assertThat(result).isSameAs(authenticationInfo);
  }
}
