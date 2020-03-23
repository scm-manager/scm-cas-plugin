/*
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

import com.cloudogu.scm.cas.CasContext;
import com.cloudogu.scm.cas.ServiceUrlProvider;
import com.google.common.base.Strings;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import sonia.scm.Priority;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.filter.Filters;
import sonia.scm.filter.WebElement;
import sonia.scm.security.Authentications;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.UserAgentParser;
import sonia.scm.web.filter.HttpFilter;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@WebElement("/*")
@Priority(Filters.PRIORITY_POST_AUTHENTICATION)
public class ForceCasLoginFilter extends HttpFilter {

  private final ServiceUrlProvider serviceUrlProvider;
  private final CasContext context;
  private final ScmConfiguration configuration;
  private final UserAgentParser userAgentParser;

  @Inject
  public ForceCasLoginFilter(ServiceUrlProvider serviceUrlProvider, CasContext context, ScmConfiguration configuration, UserAgentParser userAgentParser) {
    this.serviceUrlProvider = serviceUrlProvider;
    this.context = context;
    this.configuration = configuration;
    this.userAgentParser = userAgentParser;
  }

  @Override
  protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    if (shouldPassThrough(request)) {
      chain.doFilter(request, response);
    } else if (isWebInterfaceRequest(request)) {
      sendUnauthorized(response);
    } else {
      redirectToCas(response);
    }
  }

  private void redirectToCas(HttpServletResponse response) throws IOException {
    response.sendRedirect(createCasLoginRedirect());
  }

  private void sendUnauthorized(HttpServletResponse response) throws IOException {
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
  }

  private boolean isWebInterfaceRequest(HttpServletRequest request) {
    return "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
  }

  private boolean shouldPassThrough(HttpServletRequest request) {
    Subject subject = SecurityUtils.getSubject();
    return subject.isAuthenticated() && !Authentications.isAuthenticatedSubjectAnonymous()
      || isCasAuthenticationDisabled()
      || isCasCallback(request)
      || isAnonymousProtocolRequest(request);
  }

  private boolean isAnonymousProtocolRequest(HttpServletRequest request) {
    return !HttpUtil.isWUIRequest(request)
      && Authentications.isAuthenticatedSubjectAnonymous()
      && configuration.isAnonymousAccessEnabled()
      && !userAgentParser.parse(request).isBrowser();
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

  private String createCasLoginRedirect() {
    String encodedServiceUrl = HttpUtil.encode(serviceUrlProvider.create());
    return HttpUtil.append(context.get().getCasUrl(), "login") + "?service=" + encodedServiceUrl;
  }

}
