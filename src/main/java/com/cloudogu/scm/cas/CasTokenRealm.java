package com.cloudogu.scm.cas;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;
import sonia.scm.security.SyncingRealmHelper;
import sonia.scm.user.User;

import javax.inject.Inject;
import javax.inject.Singleton;

@Extension
@Singleton
public class CasTokenRealm extends AuthenticatingRealm {

  private static final Logger LOG = LoggerFactory.getLogger(CasTokenRealm.class);

  private final CasTokenValidator tokenValidator;
  private final AssertionMapper assertionMapper;
  private final SyncingRealmHelper syncingRealmHelper;

  @Inject
  public CasTokenRealm(CasTokenValidator tokenValidator, AssertionMapper assertionMapper, SyncingRealmHelper syncingRealmHelper) {
    this.tokenValidator = tokenValidator;
    this.assertionMapper = assertionMapper;
    this.syncingRealmHelper = syncingRealmHelper;

    setAuthenticationTokenClass(CasToken.class);
    setCredentialsMatcher(new AllowAllCredentialsMatcher());
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    CasToken casToken = (CasToken) token;

    Assertion assertion = tokenValidator.validate(casToken);
    User user = assertionMapper.createUser(assertion);

    syncingRealmHelper.store(user);

    return syncingRealmHelper.createAuthenticationInfo("cas", user);
  }

}
