package com.cloudogu.scm.cas;

import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TicketValidatorFactoryTest {

  @Test
  void shouldReturnCas30TicketValidator() {
    Configuration configuration = new Configuration();
    configuration.setCasUrl("https://hitchhiker.com/cas");

    TicketValidatorFactory factory = new TicketValidatorFactory(configuration);
    TicketValidator ticketValidator = factory.create();

    assertThat(ticketValidator).isInstanceOf(Cas30ServiceTicketValidator.class);
  }


}
