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

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.event.ScmEventBus;
import sonia.scm.security.LogoutEvent;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutHandlerTest {

  private static final String CAS_TICKET = "ST-8-L66D4LTpMGDptQ7kLark-f77b8125e3a6";

  @Mock
  private TicketStore ticketStore;

  @Mock
  private ScmEventBus eventBus;

  @InjectMocks
  private LogoutHandler handler;

  @Test
  void shouldLogout() throws IOException {
    logout();
  }

  @Test
  void shouldNotFireLogoutEventWithoutSubject() throws IOException {
    when(ticketStore.logout(CAS_TICKET)).thenReturn(Optional.empty());

    logout();

    verifyNoInteractions(eventBus);
  }

  @Test
  void shouldFireLogoutEvent() throws IOException {
    when(ticketStore.logout(CAS_TICKET)).thenReturn(Optional.of("trillian"));

    logout();

    verify(eventBus).post(new LogoutEvent("trillian"));
  }

  private void logout() throws IOException {
    String logoutRequest = read();

    handler.logout(logoutRequest);

    verify(ticketStore).logout(CAS_TICKET);
  }

  @SuppressWarnings("UnstableApiUsage")
  private String read() throws IOException {
    URL resource = Resources.getResource("com/cloudogu/scm/cas/browser/slo.xml");
    return Resources.toString(resource, Charsets.UTF_8);
  }

}
