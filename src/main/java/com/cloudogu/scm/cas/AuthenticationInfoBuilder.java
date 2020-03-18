/**
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
package com.cloudogu.scm.cas;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import sonia.scm.security.SyncingRealmHelper;
import sonia.scm.user.User;

import javax.inject.Inject;
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
