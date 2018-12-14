package com.cloudogu.scm.cas;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.SyncingRealmHelper;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationInfoBuilderTest {

  private static final String SERVICE_URL = "https://scm.hitchhiker.com";

  @Mock
  private TicketValidatorFactory ticketValidatorFactory;

  @Mock
  private AssertionMapper assertionMapper;

  @Mock
  private SyncingRealmHelper syncingRealmHelper;

  @InjectMocks
  private AuthenticationInfoBuilder authenticationInfoBuilder;

  @Mock
  private TicketValidator ticketValidator;

  @Mock
  private Assertion assertion;

  @Mock
  private AuthenticationInfo authenticationInfo;

  @BeforeEach
  void prepareMocks() {
    when(ticketValidatorFactory.create()).thenReturn(ticketValidator);
  }

  @Test
  void shouldCreateAuthenticationInfo() throws TicketValidationException {
    when(ticketValidator.validate("ST-123", SERVICE_URL)).thenReturn(assertion);

    User trillian = UserTestData.createTrillian();
    when(assertionMapper.createUser(assertion)).thenReturn(trillian);

    when(syncingRealmHelper.createAuthenticationInfo("cas", trillian)).thenReturn(authenticationInfo);

    AuthenticationInfo result = authenticationInfoBuilder.create("ST-123", SERVICE_URL);

    verify(syncingRealmHelper).store(trillian);
    assertThat(result).isSameAs(authenticationInfo);
  }

  @Test
  void shouldThrowAuthenticationException() throws TicketValidationException {
    when(ticketValidator.validate("ST-456", SERVICE_URL)).thenThrow(TicketValidationException.class);

    assertThrows(AuthenticationException.class, () -> authenticationInfoBuilder.create("ST-456", SERVICE_URL));
  }

}
