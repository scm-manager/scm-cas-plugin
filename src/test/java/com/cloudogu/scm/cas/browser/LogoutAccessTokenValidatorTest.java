/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.scm.cas.browser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.AccessToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutAccessTokenValidatorTest {

  @Mock
  private TicketStore ticketStore;

  @Mock
  private AccessToken accessToken;

  @InjectMocks
  private LogoutAccessTokenValidator validator;

  @Test
  void shouldReturnFalseIfTheIdIsBlacklisted() {
    when(ticketStore.isBlacklisted("123")).thenReturn(true);

    when(accessToken.getId()).thenReturn("123");

    boolean result = validator.validate(accessToken);
    assertFalse(result);
  }

  @Test
  void shouldReturnFalseIfTheParentIdIsBlacklisted() {
    when(ticketStore.isBlacklisted("456")).thenReturn(true);

    when(accessToken.getParentKey()).thenReturn(Optional.of("456"));
    when(accessToken.getId()).thenReturn("123");

    boolean result = validator.validate(accessToken);
    assertFalse(result);
  }

  @Test
  void shouldReturnTrueIfTheTokenIsNotBlacklisted() {
    when(accessToken.getId()).thenReturn("123");

    boolean result = validator.validate(accessToken);
    assertTrue(result);
  }

}
