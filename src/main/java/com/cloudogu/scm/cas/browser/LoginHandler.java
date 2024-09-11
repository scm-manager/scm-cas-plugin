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

package com.cloudogu.scm.cas.browser;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilder;
import sonia.scm.security.AccessTokenBuilderFactory;
import sonia.scm.security.AccessTokenCookieIssuer;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    Subject subject = SecurityUtils.getSubject();
    subject.login(token);

    PrincipalCollection principals = subject.getPrincipals();

    AccessTokenBuilder accessTokenBuilder = tokenBuilderFactory.create();
    accessTokenBuilder.subject(principals.getPrimaryPrincipal().toString());

    AccessToken accessToken = accessTokenBuilder.build();
    ticketStore.login(token, accessToken);

    cookieIssuer.authenticate(request, response, accessToken);
  }
}
