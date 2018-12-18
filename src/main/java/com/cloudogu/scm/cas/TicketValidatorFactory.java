package com.cloudogu.scm.cas;

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
    return new Cas30ServiceTicketValidator(context.get().getCasUrl());
  }
}
