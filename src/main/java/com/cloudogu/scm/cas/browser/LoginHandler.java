package com.cloudogu.scm.cas.browser;

import org.apache.shiro.SecurityUtils;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilder;
import sonia.scm.security.AccessTokenBuilderFactory;
import sonia.scm.security.AccessTokenCookieIssuer;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginHandler {

  private final AccessTokenBuilderFactory tokenBuilderFactory;
  private final TicketStore ticketStore;
  private final AccessTokenCookieIssuer cookieIssuer;

  @Inject
  public LoginHandler(AccessTokenBuilderFactory tokenBuilderFactory, TicketStore ticketStore, AccessTokenCookieIssuer cookieIssuer) {
    this.tokenBuilderFactory = tokenBuilderFactory;
    this.ticketStore = ticketStore;
    this.cookieIssuer = cookieIssuer;
  }

  public void login(HttpServletRequest request, HttpServletResponse response, CasToken token) {
    SecurityUtils.getSubject().login(token);

    AccessTokenBuilder accessTokenBuilder = tokenBuilderFactory.create();
    AccessToken accessToken = accessTokenBuilder.build();

    ticketStore.login(token, accessToken);

    cookieIssuer.authenticate(request, response, accessToken);
  }
}
