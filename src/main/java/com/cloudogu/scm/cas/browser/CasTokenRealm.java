package com.cloudogu.scm.cas.browser;

import com.cloudogu.scm.cas.AuthenticationInfoBuilder;
import com.cloudogu.scm.cas.ServiceUrlProvider;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.realm.AuthenticatingRealm;
import sonia.scm.plugin.Extension;

import javax.inject.Inject;
import javax.inject.Singleton;

@Extension
@Singleton
public class CasTokenRealm extends AuthenticatingRealm {

  private final ServiceUrlProvider serviceUrlProvider;
  private final AuthenticationInfoBuilder authenticationInfoBuilder;

  @Inject
  public CasTokenRealm(AuthenticationInfoBuilder authenticationInfoBuilder, ServiceUrlProvider serviceUrlProvider) {
    this.authenticationInfoBuilder = authenticationInfoBuilder;
    this.serviceUrlProvider = serviceUrlProvider;

    setAuthenticationTokenClass(CasToken.class);
    setCredentialsMatcher(new AllowAllCredentialsMatcher());
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
    CasToken casToken = (CasToken) token;

    String serviceUrl = serviceUrlProvider.createFromToken(casToken);
    return authenticationInfoBuilder.create(casToken.getCredentials(), serviceUrl);
  }

}
