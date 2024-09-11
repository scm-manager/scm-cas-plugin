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

import com.cloudogu.scm.cas.CasContext;
import com.cloudogu.scm.cas.Configuration;
import sonia.scm.api.v2.resources.LogoutRedirection;
import sonia.scm.plugin.Extension;

import jakarta.inject.Inject;
import java.net.URI;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Extension
public class AfterLogoutRedirectToCas implements LogoutRedirection {

  private final CasContext context;

  @Inject
  public AfterLogoutRedirectToCas(CasContext context) {
    this.context = context;
  }

  @Override
  public Optional<URI> afterLogoutRedirectTo() {
    Configuration configuration = context.get();
    if (configuration.isEnabled()) {
      String casBaseUrl = configuration.getCasUrl();
      if (!casBaseUrl.endsWith("/")) {
        casBaseUrl = casBaseUrl + "/";
      }
      return of(URI.create(casBaseUrl).resolve("./logout"));
    } else {
      return empty();
    }
  }
}
