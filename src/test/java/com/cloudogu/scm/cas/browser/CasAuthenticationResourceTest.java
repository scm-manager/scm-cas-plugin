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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sonia.scm.security.CipherHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Response;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CasAuthenticationResourceTest {

  @Captor
  private ArgumentCaptor<CasToken> tokenCaptor;

  @Mock
  private LoginHandler loginHandler;

  @Mock
  private LogoutHandler logoutHandler;

  @Mock
  private CipherHandler cipherHandler;

  @InjectMocks
  private CasAuthenticationResource casAuthenticationResource;

  @Mock
  private HttpServletRequest servletRequest;

  @Mock
  private HttpServletResponse servletResponse;

  @BeforeEach
  void setUpMocks() {
    when(servletRequest.getContextPath()).thenReturn("/scm");
    when(servletRequest.getRequestURL()).thenReturn(new StringBuffer("http://hitchhiker.com/scm/repos"));
    when(servletRequest.getRequestURI()).thenReturn("/scm/repos");
  }

  @Test
  void shouldLoginAndRedirect() {
    when(cipherHandler.decode("__enc__")).thenReturn("/repos");

    Response response = casAuthenticationResource.login(servletRequest, this.servletResponse, "__enc__", "TGT-123");

    verify(loginHandler).login(any(HttpServletRequest.class), any(HttpServletResponse.class), tokenCaptor.capture());

    CasToken token = tokenCaptor.getValue();
    assertThat(token.getUrlSuffix()).isEqualTo("__enc__");
    assertThat(token.getCredentials()).isEqualTo("TGT-123");
    assertThat(response.getLocation()).isEqualTo(URI.create("http://hitchhiker.com/scm/repos"));
  }

  @Test
  void shouldLogout() {
    Response response = casAuthenticationResource.logout("awesomeLogoutRequest");
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    verify(logoutHandler).logout("awesomeLogoutRequest");
  }

}
