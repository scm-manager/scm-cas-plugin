package com.cloudogu.scm.cas;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.realm.AuthenticatingRealm;
import sonia.scm.plugin.Extension;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
@Extension
public class CasRestRealm extends AuthenticatingRealm {

  private final AuthenticationInfoBuilder authenticationInfoBuilder;
  private final Provider<CasRestClient> restClientProvider;
  private final ServiceUrlProvider serviceUrlProvider;

  @Inject
  public CasRestRealm(AuthenticationInfoBuilder authenticationInfoBuilder, Provider<CasRestClient> restClientProvider, ServiceUrlProvider serviceUrlProvider) {
    this.authenticationInfoBuilder = authenticationInfoBuilder;
    this.restClientProvider = restClientProvider;
    this.serviceUrlProvider = serviceUrlProvider;

    setAuthenticationTokenClass(UsernamePasswordToken.class);
    setCredentialsMatcher(new AllowAllCredentialsMatcher());
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) {
    UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;

    CasRestClient restClient = restClientProvider.get();
    String grantingTicketUrl = restClient.requestGrantingTicketUrl(token.getUsername(), new String(token.getPassword()));

    String serviceUrl = serviceUrlProvider.create();
    String serviceTicket = restClient.requestServiceTicket(grantingTicketUrl, serviceUrl);

    return authenticationInfoBuilder.create(serviceTicket, serviceUrl);
  }
}
