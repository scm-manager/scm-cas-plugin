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
package com.cloudogu.scm.cas.rest;

import com.cloudogu.scm.cas.AuthenticationInfoBuilder;
import com.cloudogu.scm.cas.CasContext;
import com.cloudogu.scm.cas.ServiceUrlProvider;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.plugin.Extension;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

@Singleton
@Extension
public class CasRestRealm extends AuthenticatingRealm {

  private static final Logger LOG = LoggerFactory.getLogger(CasRestRealm.class);
  private static final String CACHE_NAME = "sonia.scm.cas.authentication";

  private final CasContext context;
  private final AuthenticationInfoBuilder authenticationInfoBuilder;
  private final Provider<CasRestClient> restClientProvider;
  private final ServiceUrlProvider serviceUrlProvider;
  private final InvalidCredentialsCache invalidCredentialsCache;

  @Inject
  public CasRestRealm(CasContext context,
                      AuthenticationInfoBuilder authenticationInfoBuilder,
                      Provider<CasRestClient> restClientProvider,
                      ServiceUrlProvider serviceUrlProvider,
                      CacheManager cacheManager,
                      InvalidCredentialsCache invalidCredentialsCache) {
    this.context = context;
    this.authenticationInfoBuilder = authenticationInfoBuilder;
    this.restClientProvider = restClientProvider;
    this.serviceUrlProvider = serviceUrlProvider;
    this.invalidCredentialsCache = invalidCredentialsCache;

    setAuthenticationTokenClass(UsernamePasswordToken.class);
    setCredentialsMatcher(new AllowAllCredentialsMatcher());

    Cache<Object, AuthenticationInfo> cache = cacheManager.getCache(CACHE_NAME);
    setAuthenticationCache(cache);
    setAuthenticationCachingEnabled(true);
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) {
    if (!context.get().isEnabled()) {
      LOG.debug("cas authentication is disabled, skipping cas rest realm");
      return null;
    }

    UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
    invalidCredentialsCache.verifyNotInvalid(token);

    CasRestClient restClient = restClientProvider.get();
    String grantingTicketUrl;
    try {
      grantingTicketUrl = restClient.requestGrantingTicketUrl(token.getUsername(), new String(token.getPassword()));
    } catch (AuthenticationException e) {
      invalidCredentialsCache.cacheAsInvalid(token);
      throw e;
    }

    String serviceUrl = serviceUrlProvider.create();
    String serviceTicket = restClient.requestServiceTicket(grantingTicketUrl, serviceUrl);

    return authenticationInfoBuilder.create(serviceTicket, serviceUrl);
  }
}
