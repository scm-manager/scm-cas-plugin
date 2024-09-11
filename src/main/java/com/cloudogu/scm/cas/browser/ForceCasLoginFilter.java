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
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import sonia.scm.Priority;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.filter.Filters;
import sonia.scm.filter.WebElement;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.security.AnonymousMode;
import sonia.scm.security.Authentications;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.UserAgentParser;
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
  private final ScmConfiguration configuration;
  private final UserAgentParser userAgentParser;
  private final AccessTokenCookieIssuer accessTokenCookieIssuer;

  @Inject
  public ForceCasLoginFilter(ServiceUrlProvider serviceUrlProvider, CasContext context, ScmConfiguration configuration, UserAgentParser userAgentParser, AccessTokenCookieIssuer accessTokenCookieIssuer) {
    this.serviceUrlProvider = serviceUrlProvider;
    this.context = context;
    this.configuration = configuration;
    this.userAgentParser = userAgentParser;
    this.accessTokenCookieIssuer = accessTokenCookieIssuer;
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
    return isUserAuthenticated()
      || isCasAuthenticationDisabled()
      || isCasCallback(request)
      || isAnonymousProtocolRequest(request)
      || isMercurialHookRequest(request)
      || (!isLoginRequest(request) && isFullAnonymousAccessEnabled());
  }

  private boolean isMercurialHookRequest(HttpServletRequest request) {
    return request.getRequestURI().startsWith(request.getContextPath() + "/hook/hg/");
  }

  private boolean isLoginRequest(HttpServletRequest request) {
    final String requestURI = request.getRequestURI();
    final String contextPath = request.getContextPath();
    return requestURI != null && requestURI.startsWith(contextPath + "/login");
  }

  private boolean isUserAuthenticated() {
    Subject subject = SecurityUtils.getSubject();
    return subject.isAuthenticated() && !Authentications.isAuthenticatedSubjectAnonymous();
  }

  private boolean isAnonymousProtocolRequest(HttpServletRequest request) {
    return !HttpUtil.isWUIRequest(request)
      && Authentications.isAuthenticatedSubjectAnonymous()
      && configuration.getAnonymousMode() == AnonymousMode.PROTOCOL_ONLY
      && !userAgentParser.parse(request).isBrowser();
  }

  private boolean isFullAnonymousAccessEnabled() {
    return configuration.getAnonymousMode() == AnonymousMode.FULL;
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
