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

import com.google.common.base.Strings;
import org.apache.shiro.authc.AuthenticationToken;

import static com.google.common.base.Preconditions.checkArgument;

public final class CasToken implements AuthenticationToken {

  private final String ticket;
  private final String urlSuffix;

  private CasToken(String ticket, String urlSuffix) {
    this.ticket = ticket;
    this.urlSuffix = urlSuffix;
  }

  @Override
  public String getCredentials() {
    return ticket;
  }

  public String getUrlSuffix() {
    return urlSuffix;
  }

  @Override
  public Object getPrincipal() {
    throw new UnsupportedOperationException("CasToken has principal, it provides only credentials");
  }

  public static CasToken valueOf(String ticket, String urlSuffix) {
    checkArgument(!Strings.isNullOrEmpty(ticket), "ticket is null or empty");
    checkArgument(!Strings.isNullOrEmpty(urlSuffix), "urlSuffix is null or empty");
    return new CasToken(ticket, urlSuffix);
  }
}
