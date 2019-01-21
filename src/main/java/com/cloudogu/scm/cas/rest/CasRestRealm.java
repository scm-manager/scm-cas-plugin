package com.cloudogu.scm.cas.rest;

import com.cloudogu.scm.cas.AuthenticationInfoBuilder;
import com.cloudogu.scm.cas.CasContext;
import com.cloudogu.scm.cas.ServiceUrlProvider;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
@Extension
public class CasRestRealm extends AuthenticatingRealm {

  private static final Logger LOG = LoggerFactory.getLogger(CasRestRealm.class);

  private final CasContext context;
  private final AuthenticationInfoBuilder authenticationInfoBuilder;
  private final Provider<CasRestClient> restClientProvider;
  private final ServiceUrlProvider serviceUrlProvider;

  @Inject
  public CasRestRealm(CasContext context, AuthenticationInfoBuilder authenticationInfoBuilder, Provider<CasRestClient> restClientProvider, ServiceUrlProvider serviceUrlProvider) {
    this.context = context;
    this.authenticationInfoBuilder = authenticationInfoBuilder;
    this.restClientProvider = restClientProvider;
    this.serviceUrlProvider = serviceUrlProvider;

    setAuthenticationTokenClass(UsernamePasswordToken.class);
    setCredentialsMatcher(new AllowAllCredentialsMatcher());
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) {
    if (!context.get().isEnabled()) {
      LOG.debug("cas authentication is disabled, skipping cas rest realm");
      return null;
    }

    UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;

    CasRestClient restClient = restClientProvider.get();
    String grantingTicketUrl = restClient.requestGrantingTicketUrl(token.getUsername(), new String(token.getPassword()));

    String serviceUrl = serviceUrlProvider.create();
    String serviceTicket = restClient.requestServiceTicket(grantingTicketUrl, serviceUrl);

    return authenticationInfoBuilder.create(serviceTicket, serviceUrl);
  }
}
