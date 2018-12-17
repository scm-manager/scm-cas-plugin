package com.cloudogu.scm.cas.browser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutAccessTokenValidatorTest {

  @Mock
  private TicketStore ticketStore;

  @InjectMocks
  private LogoutAccessTokenValidator validator;

  @Test
  void shouldReturnFalseIfTheIdIsBlacklisted() {
    when(ticketStore.isBlacklisted("123")).thenReturn(true);

    Map<String,Object> claims = new HashMap<>();
    claims.put("jti", "123");

    boolean result = validator.validate(claims);
    assertFalse(result);
  }

  @Test
  void shouldReturnFalseIfTheParentIdIsBlacklisted() {
    when(ticketStore.isBlacklisted("456")).thenReturn(true);

    Map<String,Object> claims = new HashMap<>();
    claims.put("scm-manager.parentTokenId", "456");
    claims.put("jti", "123");

    boolean result = validator.validate(claims);
    assertFalse(result);
  }

  @Test
  void shouldReturnTrueIfTheTokenIsNotBlacklisted() {
    Map<String,Object> claims = new HashMap<>();
    claims.put("jti", "123");

    boolean result = validator.validate(claims);
    assertTrue(result);
  }

}
