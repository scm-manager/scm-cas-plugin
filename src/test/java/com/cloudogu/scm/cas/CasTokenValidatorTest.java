package com.cloudogu.scm.cas;

import org.apache.shiro.authc.AuthenticationException;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CasTokenValidatorTest {

  private static final String SERVICE_URL = "https://scm.hitchhiker.com";

  @Mock
  private ServiceUrlProvider serviceUrlProvider;

  @Mock
  private TicketValidatorFactory ticketValidatorFactory;

  @Mock
  private TicketValidator ticketValidator;

  @Mock
  private Assertion assertion;

  private CasTokenValidator tokenValidator;

  @BeforeEach
  void setUpObjectUnderTest() {
    when(ticketValidatorFactory.create()).thenReturn(ticketValidator);

    tokenValidator = new CasTokenValidator(serviceUrlProvider, ticketValidatorFactory);
  }

  @Test
  void shouldReturnAssertionsForCasToken() throws TicketValidationException {
    when(ticketValidator.validate("TGT-123", SERVICE_URL)).thenReturn(assertion);

    CasToken casToken = CasToken.valueOf("TGT-123", "__enc__");
    when(serviceUrlProvider.createFromToken(casToken)).thenReturn(SERVICE_URL);

    Assertion validatedAssertions = tokenValidator.validate(casToken);
    assertThat(validatedAssertions).isSameAs(assertion);
  }

  @Test
  void shouldThrowAuthenticationExceptionIfTokenCouldNotBeValidated() throws TicketValidationException {
    CasToken casToken = CasToken.valueOf("TGT-123", "__enc__");
    when(serviceUrlProvider.createFromToken(casToken)).thenReturn(SERVICE_URL);

    when(ticketValidator.validate("TGT-123", SERVICE_URL)).thenThrow(TicketValidationException.class);

    assertThrows(AuthenticationException.class, () -> tokenValidator.validate(casToken));
  }
}
