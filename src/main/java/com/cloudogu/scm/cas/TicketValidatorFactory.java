package com.cloudogu.scm.cas;

import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;

import javax.inject.Inject;

public class TicketValidatorFactory {

  private Configuration configuration;

  @Inject
  public TicketValidatorFactory(Configuration configuration) {
    this.configuration = configuration;
  }

  public TicketValidator create() {
    return new Cas30ServiceTicketValidator(configuration.getCasUrl());
  }
}
