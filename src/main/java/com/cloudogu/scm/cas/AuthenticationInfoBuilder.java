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

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import sonia.scm.security.SyncingRealmHelper;
import sonia.scm.user.User;

import jakarta.inject.Inject;
import java.util.Set;

public class AuthenticationInfoBuilder {

  private final TicketValidatorFactory ticketValidatorFactory;
  private final AssertionMapper assertionMapper;
  private final SyncingRealmHelper syncingRealmHelper;
  private final GroupStore groupStore;

  @Inject
  public AuthenticationInfoBuilder(TicketValidatorFactory ticketValidatorFactory, AssertionMapper assertionMapper, SyncingRealmHelper syncingRealmHelper, GroupStore groupStore) {
    this.ticketValidatorFactory = ticketValidatorFactory;
    this.assertionMapper = assertionMapper;
    this.syncingRealmHelper = syncingRealmHelper;
    this.groupStore = groupStore;
  }

  public AuthenticationInfo create(String serviceTicket, String serviceUrl) {
    Assertion assertion = validate(serviceTicket, serviceUrl);

    User user = assertionMapper.createUser(assertion);
    syncingRealmHelper.store(user);

    Set<String> groups = assertionMapper.createGroups(assertion);
    groupStore.put(user.getName(), groups);

    return syncingRealmHelper.createAuthenticationInfo(Constants.NAME, user);
  }

  private Assertion validate(String serviceTicket, String serviceUrl) {
    try {
      TicketValidator ticketValidator = ticketValidatorFactory.create();
      return ticketValidator.validate(serviceTicket, serviceUrl);
    } catch (TicketValidationException ex) {
      throw new AuthenticationException("failed to validate service ticket", ex);
    }
  }

}
