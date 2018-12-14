package com.cloudogu.scm.cas;

import org.apache.shiro.authc.AuthenticationInfo;
import org.jasig.cas.client.validation.Assertion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.SyncingRealmHelper;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CasTokenRealmTest {

  private static final String SERVICE_URL = "https://scm.hitchhiker.com/scm";

  @Mock
  private ServiceUrlProvider serviceUrlProvider;

  @Mock
  private AuthenticationInfoBuilder authenticationInfoBuilder;

  @InjectMocks
  private CasTokenRealm realm;

  @Mock
  private AuthenticationInfo authenticationInfo;

  @Test
  void shouldReturnAuthenticationInfo() {
    CasToken token = CasToken.valueOf("TGT-123", "anc");

    when(serviceUrlProvider.createFromToken(token)).thenReturn(SERVICE_URL);
    when(authenticationInfoBuilder.create("TGT-123", SERVICE_URL)).thenReturn(authenticationInfo);

    AuthenticationInfo result = realm.doGetAuthenticationInfo(token);
    assertThat(result).isSameAs(authenticationInfo);
  }

}
