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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilder;
import sonia.scm.security.AccessTokenBuilderFactory;
import sonia.scm.security.CipherHandler;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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

  @Mock
  private AccessTokenBuilderFactory accessTokenBuilderFactory;

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

  @Nested
  class AccessTokenTests {

    @Mock(answer = Answers.RETURNS_SELF)
    private AccessTokenBuilder accessTokenBuilder;
    @Mock
    private AccessToken accessToken;

    @BeforeEach
    void mockAccessTokenBuilderFactory() {
      when(accessTokenBuilderFactory.create()).thenReturn(accessTokenBuilder);
    }

    @Test
    void shouldCreateAccessToken() {
      when(accessTokenBuilder.build())
        .thenReturn(accessToken);
      when(accessToken.compact()).thenReturn("myBearerToken");

      Response response = casAuthenticationResource.accessToken(
        servletRequest,
        servletResponse,
        "myTicket",
        null
      );

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
      assertThat(response.getEntity()).isEqualTo("myBearerToken");
      verify(accessTokenBuilder, never()).scope(any());
      verify(loginHandler)
        .login(
          eq(servletRequest),
          eq(servletResponse),
          argThat(token -> {
            assertThat(token.getCredentials()).isEqualTo("myTicket");
            assertThat(token.getUrlSuffix()).isEqualTo("/");
            return true;
          })
        );
    }

    @Test
    void shouldCreateScopedAccessToken() {
      when(accessTokenBuilder.build())
        .thenReturn(accessToken);
      when(accessToken.compact()).thenReturn("myBearerToken");

      Response response = casAuthenticationResource.accessToken(
        servletRequest,
        servletResponse,
        "myTicket",
        "repository:read:*"
      );

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
      assertThat(response.getEntity()).isEqualTo("myBearerToken");
      verify(accessTokenBuilder).scope(argThat(scope -> {
        assertThat(scope.iterator().next()).isEqualTo("repository:read:*");
        return true;
      }));
      verify(loginHandler)
        .login(
          eq(servletRequest),
          eq(servletResponse),
          argThat(token -> {
            assertThat(token.getCredentials()).isEqualTo("myTicket");
            assertThat(token.getUrlSuffix()).isEqualTo("/");
            return true;
          })
        );
    }
  }
}
