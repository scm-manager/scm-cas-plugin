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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenValidator;

import jakarta.inject.Inject;

@Extension
public class LogoutAccessTokenValidator implements AccessTokenValidator {

  private static final Logger LOG = LoggerFactory.getLogger(LogoutAccessTokenValidator.class);

  private final TicketStore ticketStore;

  @Inject
  public LogoutAccessTokenValidator(TicketStore ticketStore) {
    this.ticketStore = ticketStore;
  }

  @Override
  public boolean validate(AccessToken token) {
    LOG.trace("checking whether token {} with parent id {} is valid", token.getId(), token.getParentKey());
    String id = resolveId(token);
    return !ticketStore.isBlacklisted(id);
  }

  private String resolveId(AccessToken token) {
    return token.getParentKey().orElse(token.getId());
  }
}
