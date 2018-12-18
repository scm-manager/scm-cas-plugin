package com.cloudogu.scm.cas;

import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketValidatorFactoryTest {

  @Mock
  private CasContext casContext;

  @Test
  void shouldReturnCas30TicketValidator() {
    Configuration configuration = new Configuration();
    configuration.setCasUrl("https://hitchhiker.com/cas");

    when(casContext.get()).thenReturn(configuration);

    TicketValidatorFactory factory = new TicketValidatorFactory(casContext);
    TicketValidator ticketValidator = factory.create();

    assertThat(ticketValidator).isInstanceOf(Cas30ServiceTicketValidator.class);
  }


}
