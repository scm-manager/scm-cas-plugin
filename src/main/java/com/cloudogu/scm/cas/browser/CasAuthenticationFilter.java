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
import sonia.scm.Priority;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.filter.Filters;
import sonia.scm.filter.WebElement;
import sonia.scm.security.AnonymousToken;
import sonia.scm.security.TokenExpiredException;
import sonia.scm.security.TokenValidationFailedException;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.WebTokenGenerator;
import sonia.scm.web.filter.AuthenticationFilter;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

@WebElement("/*")
@Priority(Filters.PRIORITY_AUTHENTICATION)
public class CasAuthenticationFilter extends AuthenticationFilter {

  @Inject
  public CasAuthenticationFilter(ScmConfiguration configuration, Set<WebTokenGenerator> tokenGenerators) {
    super(configuration, tokenGenerators);
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
    if (tokenValidationFailedException.getValidator().getClass() == LogoutAccessTokenValidator.class) {
      SecurityUtils.getSubject().login(new AnonymousToken());
      chain.doFilter(new ExcludeCookieRequestWrapper(request), response);
    } else {
      super.handleTokenValidationFailedException(request, response, chain, tokenValidationFailedException);
    }
  }

  private static class ExcludeCookieRequestWrapper extends HttpServletRequestWrapper {
    public ExcludeCookieRequestWrapper(HttpServletRequest request) {
      super(request);
    }

    @Override
    public Cookie[] getCookies() {
      return Arrays.stream(super.getCookies()).filter(cookie -> !cookie.getName().equals(HttpUtil.COOKIE_BEARER_AUTHENTICATION)).toArray(Cookie[]::new);
    }
  }
}
