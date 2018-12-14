package com.cloudogu.scm.cas;

import org.apache.shiro.authc.AuthenticationException;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;

import javax.inject.Inject;

public class CasTokenValidator {

  private final ServiceUrlProvider serviceUrlProvider;
  private final TicketValidatorFactory ticketValidatorFactory;

  @Inject
  public CasTokenValidator(ServiceUrlProvider serviceUrlProvider, TicketValidatorFactory ticketValidatorFactory) {
    this.serviceUrlProvider = serviceUrlProvider;
    this.ticketValidatorFactory = ticketValidatorFactory;
  }

  public Assertion validate(CasToken casToken) throws AuthenticationException {
    TicketValidator validator = ticketValidatorFactory.create();
    String serviceUrl = serviceUrlProvider.createFromToken(casToken);
    try {
      return validator.validate(casToken.getCredentials(), serviceUrl);
    } catch (TicketValidationException ex) {
      throw new AuthenticationException("failed to validate ticket", ex);
    }
  }
}
