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

package com.cloudogu.scm.cas;

import com.google.inject.Inject;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

public class RequestHolder {

  private Provider<HttpServletRequest> requestProvider;

  @Inject
  public RequestHolder(Provider<HttpServletRequest> requestProvider) {
    this.requestProvider = requestProvider;
  }

  public Optional<HttpServletRequest> getRequest() {
    try {
      return Optional.of(requestProvider.get());
    } catch (ProvisionException ex) {
      if (ex.getCause() instanceof OutOfScopeException) {
        return Optional.empty();
      } else {
        throw ex;
      }
    }
  }
}
