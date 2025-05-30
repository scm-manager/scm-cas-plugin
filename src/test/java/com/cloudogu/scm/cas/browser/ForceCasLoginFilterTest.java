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

import com.cloudogu.scm.cas.CasContext;
import com.cloudogu.scm.cas.Configuration;
import com.cloudogu.scm.cas.ServiceUrlProvider;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.security.ShouldRequestPassChecker;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ForceCasLoginFilterTest {

  private static final String CAS_URL = "https://sso.hitchhiker.com";
  private static final String CAS_LOGIN_URL = CAS_URL.concat("/login");
  private static final String SERVICE_URL = "https://scm.hitchhiker.com/repo/hitchhiker/deep-thought";
  private static final String SERVICE_URL_ESCAPED = "https%3A%2F%2Fscm.hitchhiker.com%2Frepo%2Fhitchhiker%2Fdeep-thought";

  @Mock
  private ServiceUrlProvider serviceUrlProvider;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain chain;

  @Mock
  private Subject subject;

  @Mock
  private CasContext casContext;

  @Mock
  private ShouldRequestPassChecker passChecker;

  @Mock
  private AccessTokenCookieIssuer accessTokenCookieIssuer;

  private ForceCasLoginFilter filter;

  private ThreadState subjectThreadState;

  private Configuration configuration;

  @BeforeEach
  void setUpObjectUnderTest() {
    configuration = new Configuration();
    configuration.setCasUrl(CAS_URL);
    configuration.setEnabled(true);

    when(casContext.get()).thenReturn(configuration);
    when(serviceUrlProvider.create()).thenReturn(SERVICE_URL);

    filter = new ForceCasLoginFilter(serviceUrlProvider, casContext, accessTokenCookieIssuer, passChecker);

    subjectThreadState = new SubjectThreadState(subject);
    subjectThreadState.bind();
  }

  @AfterEach
  void cleanUpAuthenticationEnvironment() {
    subjectThreadState.clear();
  }

  @Test
  void shouldRedirectToCas() throws IOException, ServletException {
    when(passChecker.shouldPass(request)).thenReturn(false);
    when(request.getRequestURI()).thenReturn("/scm/repos");

    filter.doFilter(request, response, chain);

    verify(response).sendRedirect(CAS_LOGIN_URL + "?service=" + SERVICE_URL_ESCAPED);
    verify(accessTokenCookieIssuer).invalidate(request, response);
  }

  @Test
  void shouldNotRedirectOnCasLogin() throws IOException, ServletException {
    when(request.getContextPath()).thenReturn("/scm");
    when(request.getRequestURI()).thenReturn("/scm/api/v2/cas/__enc__");
    when(request.getMethod()).thenReturn("GET");
    when(request.getParameter("ticket")).thenReturn("TGT-123");

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    verify(accessTokenCookieIssuer, never()).invalidate(request, response);
  }

  @Test
  void shouldNotRedirectOnCasLogout() throws IOException, ServletException {
    when(request.getContextPath()).thenReturn("/scm");
    when(request.getRequestURI()).thenReturn("/scm/api/v2/cas/__enc__");
    when(request.getMethod()).thenReturn("POST");
    when(request.getContentType()).thenReturn(MediaType.APPLICATION_FORM_URLENCODED);

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    verify(accessTokenCookieIssuer, never()).invalidate(request, response);
  }

  @Test
  void shouldNotRedirectIfCasIsDisabled() throws IOException, ServletException {
    configuration.setEnabled(false);

    when(request.getRequestURI()).thenReturn("/scm/repos");
    when(serviceUrlProvider.create()).thenReturn(SERVICE_URL);

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    verify(accessTokenCookieIssuer, never()).invalidate(request, response);
  }

  @Test
  void shouldSendUnauthorizedOnAjaxRequests() throws IOException, ServletException {
    when(request.getRequestURI()).thenReturn("/repos");
    when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");
    when(serviceUrlProvider.create()).thenReturn(SERVICE_URL);

    filter.doFilter(request, response, chain);

    verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    verify(accessTokenCookieIssuer, never()).invalidate(request, response);
  }
}
