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
