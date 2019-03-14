package com.cloudogu.scm.cas;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import sonia.scm.security.SyncingRealmHelper;
import sonia.scm.user.User;

import javax.inject.Inject;
import java.util.Collection;

public class AuthenticationInfoBuilder {

  private final TicketValidatorFactory ticketValidatorFactory;
  private final AssertionMapper assertionMapper;
  private final SyncingRealmHelper syncingRealmHelper;

  @Inject
  public AuthenticationInfoBuilder(TicketValidatorFactory ticketValidatorFactory, AssertionMapper assertionMapper, SyncingRealmHelper syncingRealmHelper) {
    this.ticketValidatorFactory = ticketValidatorFactory;
    this.assertionMapper = assertionMapper;
    this.syncingRealmHelper = syncingRealmHelper;
  }

  public AuthenticationInfo create(String serviceTicket, String serviceUrl) {
    Assertion assertion = validate(serviceTicket, serviceUrl);

    User user = assertionMapper.createUser(assertion);
    syncingRealmHelper.store(user);

    Collection<String> groups = assertionMapper.createGroups(assertion);

    return syncingRealmHelper.authenticationInfo()
      .forRealm(Constants.NAME)
      .andUser(user)
      .withGroups(groups)
      .build();
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
