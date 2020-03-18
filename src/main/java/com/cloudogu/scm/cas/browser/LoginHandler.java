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
package com.cloudogu.scm.cas.browser;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
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
