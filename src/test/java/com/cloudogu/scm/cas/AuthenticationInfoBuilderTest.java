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

package com.cloudogu.scm.cas;

import com.google.common.collect.ImmutableSet;
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

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

  @Mock
  private GroupStore groupStore;

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
    trillian.setPassword("currentPassword");
    when(assertionMapper.createUser(assertion)).thenReturn(trillian);

    Set<String> groups = ImmutableSet.of("heartOfGoldCrew", "earth2construction");
    when(assertionMapper.createGroups(assertion)).thenReturn(groups);

    when(syncingRealmHelper.createAuthenticationInfo(Constants.NAME, trillian)).thenReturn(authenticationInfo);

    AuthenticationInfo result = authenticationInfoBuilder.create("ST-123", SERVICE_URL, null);

    verify(syncingRealmHelper).store(trillian);
    assertThat(trillian.getPassword()).isEqualTo("currentPassword");
    assertThat(result).isSameAs(authenticationInfo);

    verify(groupStore).put(trillian.getName(), groups);
  }

  @Test
  void shouldCreateAuthenticationInfoWithPassword() throws TicketValidationException {
    String password = "password";
    when(ticketValidator.validate("ST-123", SERVICE_URL)).thenReturn(assertion);

    User trillian = UserTestData.createTrillian();
    trillian.setPassword(null);
    when(assertionMapper.createUser(assertion)).thenReturn(trillian);

    Set<String> groups = ImmutableSet.of("heartOfGoldCrew", "earth2construction");
    when(assertionMapper.createGroups(assertion)).thenReturn(groups);

    when(syncingRealmHelper.createAuthenticationInfo(Constants.NAME, trillian)).thenReturn(authenticationInfo);

    AuthenticationInfo result = authenticationInfoBuilder.create("ST-123", SERVICE_URL, password);

    verify(syncingRealmHelper).store(trillian);
    assertThat(trillian.getPassword()).isEqualTo(password);
    assertThat(result).isSameAs(authenticationInfo);

    verify(groupStore).put(trillian.getName(), groups);
  }

  @Test
  void shouldThrowAuthenticationException() throws TicketValidationException {
    when(ticketValidator.validate("ST-456", SERVICE_URL)).thenThrow(TicketValidationException.class);

    assertThrows(AuthenticationException.class, () -> authenticationInfoBuilder.create("ST-456", SERVICE_URL, null));
  }
}
