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

  @Mock
  private CasTokenValidator tokenValidator;

  @Mock
  private AssertionMapper assertionMapper;

  @Mock
  private Assertion assertion;

  @Mock
  private SyncingRealmHelper realmHelper;

  @Mock
  private AuthenticationInfo authenticationInfo;

  @InjectMocks
  private CasTokenRealm realm;


  @Test
  void shouldReturnAuthenticationInfo() {
    CasToken token = CasToken.valueOf("TGT-123", "anc");

    User trillian = UserTestData.createTrillian();

    when(tokenValidator.validate(token)).thenReturn(assertion);
    when(assertionMapper.createUser(assertion)).thenReturn(trillian);
    when(realmHelper.createAuthenticationInfo("cas", trillian)).thenReturn(authenticationInfo);

    AuthenticationInfo result = realm.doGetAuthenticationInfo(token);
    assertThat(result).isSameAs(authenticationInfo);

    verify(realmHelper).store(trillian);
  }

}
