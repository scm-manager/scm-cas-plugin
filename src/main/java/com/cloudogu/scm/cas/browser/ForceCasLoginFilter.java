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

import com.cloudogu.scm.cas.CasContext;
import com.cloudogu.scm.cas.ServiceUrlProvider;
import com.google.common.base.Strings;
import sonia.scm.Priority;
import sonia.scm.filter.Filters;
import sonia.scm.filter.WebElement;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.security.ShouldRequestPassChecker;
import sonia.scm.web.filter.HttpFilter;

import jakarta.inject.Inject;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;

import static com.cloudogu.scm.cas.CasLoginLinkProvider.createLoginLink;

@WebElement("/*")
@Priority(Filters.PRIORITY_POST_AUTHENTICATION)
public class ForceCasLoginFilter extends HttpFilter {

  private final ServiceUrlProvider serviceUrlProvider;
  private final CasContext context;
  private final AccessTokenCookieIssuer accessTokenCookieIssuer;
  private final ShouldRequestPassChecker requestPassChecker;

  @Inject
  public ForceCasLoginFilter(ServiceUrlProvider serviceUrlProvider, CasContext context, AccessTokenCookieIssuer accessTokenCookieIssuer, ShouldRequestPassChecker requestPassChecker) {
    this.serviceUrlProvider = serviceUrlProvider;
    this.context = context;
    this.accessTokenCookieIssuer = accessTokenCookieIssuer;
    this.requestPassChecker = requestPassChecker;
  }

  @Override
  protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    if (shouldPassThrough(request)) {
      chain.doFilter(request, response);
    } else if (isWebInterfaceRequest(request)) {
      sendUnauthorized(response);
    } else {
      redirectToCas(request, response);
    }
  }

  private void redirectToCas(HttpServletRequest request, HttpServletResponse response) throws IOException {
    accessTokenCookieIssuer.invalidate(request, response);
    response.sendRedirect(createLoginLink(context, serviceUrlProvider.create()));
  }

  private void sendUnauthorized(HttpServletResponse response) throws IOException {
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
  }

  private boolean isWebInterfaceRequest(HttpServletRequest request) {
    return "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
  }

  private boolean shouldPassThrough(HttpServletRequest request) {
    return requestPassChecker.shouldPass(request)
      || isCasAuthenticationDisabled()
      || isCasCallback(request);
  }

  private boolean isCasAuthenticationDisabled() {
    return !context.get().isEnabled();
  }

  private boolean isCasCallback(HttpServletRequest request) {
    return request.getRequestURI().startsWith(request.getContextPath() + "/api/v2/cas/")
      && (isCasLoginRequest(request) || isCasLogoutRequest(request));
  }

  private boolean isCasLogoutRequest(HttpServletRequest request) {
    return "POST".equals(request.getMethod()) && MediaType.APPLICATION_FORM_URLENCODED.equals(request.getContentType());
  }

  private boolean isCasLoginRequest(HttpServletRequest request) {
    return "GET".equals(request.getMethod()) && !Strings.isNullOrEmpty(request.getParameter("ticket"));
  }

}
