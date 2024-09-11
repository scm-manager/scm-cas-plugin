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
