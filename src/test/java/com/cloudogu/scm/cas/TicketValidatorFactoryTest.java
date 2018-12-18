package com.cloudogu.scm.cas;

import org.apache.shiro.authc.AuthenticationException;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketValidatorFactoryTest {

  @Mock
  private CasContext casContext;

  @Test
  void shouldReturnCas30TicketValidator() {
    bindConfiguration(true);

    TicketValidatorFactory factory = new TicketValidatorFactory(casContext);
    TicketValidator ticketValidator = factory.create();

    assertThat(ticketValidator).isInstanceOf(Cas30ServiceTicketValidator.class);
  }

  @Test
  void shouldThrowAuthenticationExceptionIfCasAuthenticationIsDisabled() {
    bindConfiguration(false);
    TicketValidatorFactory factory = new TicketValidatorFactory(casContext);

    assertThrows(AuthenticationException.class, () -> factory.create());
  }

  private void bindConfiguration(boolean b) {
    Configuration configuration = new Configuration();
    configuration.setCasUrl("https://hitchhiker.com/cas");
    configuration.setEnabled(b);

    when(casContext.get()).thenReturn(configuration);
  }


}
