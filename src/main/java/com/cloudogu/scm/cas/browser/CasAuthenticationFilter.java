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

import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.Priority;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.filter.Filters;
import sonia.scm.filter.WebElement;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.security.AnonymousToken;
import sonia.scm.security.TokenExpiredException;
import sonia.scm.security.TokenValidationFailedException;
import sonia.scm.web.WebTokenGenerator;
import sonia.scm.web.filter.AuthenticationFilter;

import jakarta.inject.Inject;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@WebElement("/*")
@Priority(Filters.PRIORITY_AUTHENTICATION)
public class CasAuthenticationFilter extends AuthenticationFilter {

  private static final Logger LOG = LoggerFactory.getLogger(CasAuthenticationFilter.class);
  private final AccessTokenCookieIssuer cookieIssuer;

  @Inject
  public CasAuthenticationFilter(ScmConfiguration configuration, Set<WebTokenGenerator> tokenGenerators, AccessTokenCookieIssuer cookieIssuer) {
    super(configuration, tokenGenerators);
    this.cookieIssuer = cookieIssuer;
  }

  @Override
  protected void handleUnauthorized(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    chain.doFilter(request, response);
  }

  @Override
  protected void handleTokenExpiredException(HttpServletRequest request, HttpServletResponse response, FilterChain chain, TokenExpiredException tokenExpiredException) throws IOException, ServletException {
    chain.doFilter(request, response);
  }

  @Override
  protected void handleTokenValidationFailedException(HttpServletRequest request, HttpServletResponse response, FilterChain chain, TokenValidationFailedException tokenValidationFailedException) throws IOException, ServletException {
    if (shouldContinueAsAnonymous(tokenValidationFailedException)) {
      LOG.debug("access token is marked as invalid by LogoutAccessTokenValidator, continue as anonymous");
      continueAsAnonymous(request, response);
    }
    chain.doFilter(request, response);
  }

  private void continueAsAnonymous(HttpServletRequest request, HttpServletResponse response) {
    cookieIssuer.invalidate(request, response);
    SecurityUtils.getSubject().login(new AnonymousToken());
  }

  private boolean shouldContinueAsAnonymous(TokenValidationFailedException tokenValidationFailedException) {
    return isAnonymousAccessEnabled()
      && tokenValidationFailedException.getValidator().getClass() == LogoutAccessTokenValidator.class;
  }
}
