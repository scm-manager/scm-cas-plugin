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

import com.cloudogu.scm.cas.AuthenticationInfoBuilder;
import com.cloudogu.scm.cas.ServiceUrlProvider;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.realm.AuthenticatingRealm;
import sonia.scm.plugin.Extension;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Extension
@Singleton
public class CasTokenRealm extends AuthenticatingRealm {

  private final ServiceUrlProvider serviceUrlProvider;
  private final AuthenticationInfoBuilder authenticationInfoBuilder;

  @Inject
  public CasTokenRealm(AuthenticationInfoBuilder authenticationInfoBuilder, ServiceUrlProvider serviceUrlProvider) {
    this.authenticationInfoBuilder = authenticationInfoBuilder;
    this.serviceUrlProvider = serviceUrlProvider;

    setAuthenticationTokenClass(CasToken.class);
    setCredentialsMatcher(new AllowAllCredentialsMatcher());
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
    CasToken casToken = (CasToken) token;

    String serviceUrl = serviceUrlProvider.createFromToken(casToken);
    return authenticationInfoBuilder.create(casToken.getCredentials(), serviceUrl);
  }

}
