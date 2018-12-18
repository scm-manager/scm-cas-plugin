package com.cloudogu.scm.cas;

import org.apache.shiro.authc.AuthenticationException;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;

import javax.inject.Inject;

public class TicketValidatorFactory {

  private final CasContext context;

  @Inject
  public TicketValidatorFactory(CasContext context) {
    this.context = context;
  }

  public TicketValidator create() {
    Configuration configuration = context.get();
    if (!configuration.isEnabled()) {
      throw new AuthenticationException("cas authentication is disabled");
    }
    return new Cas30ServiceTicketValidator(configuration.getCasUrl());
  }
}
