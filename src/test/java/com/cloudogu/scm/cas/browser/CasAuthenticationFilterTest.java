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

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.security.AccessTokenValidator;
import sonia.scm.security.AnonymousMode;
import sonia.scm.security.AnonymousToken;
import sonia.scm.security.TokenValidationFailedException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CasAuthenticationFilterTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain chain;

  @Mock
  private AccessTokenCookieIssuer cookieIssuer;

  @Mock
  private LogoutAccessTokenValidator validator;

  @Mock
  private AccessToken accessToken;

  private ScmConfiguration configuration;

  private CasAuthenticationFilter authenticationFilter;

  @BeforeEach
  void setUpObjectUnderTest() {
    configuration = new ScmConfiguration();
    authenticationFilter = new CasAuthenticationFilter(configuration, Collections.emptySet(), cookieIssuer);
  }

  @Test
  void shouldNotContinueAsAnonymousIfAnonymousModeIsNotEnabled() throws IOException, ServletException {
    authenticationFilter.handleTokenValidationFailedException(
      request, response, chain, createLogoutTokenValidationFailedException()
    );

    verify(chain).doFilter(request, response);
  }

  @Test
  void shouldNotContinueAsAnonymousIfAnotherValidatorHasFailed() throws IOException, ServletException {
    configuration.setAnonymousMode(AnonymousMode.FULL);

    authenticationFilter.handleTokenValidationFailedException(
      request, response, chain, createTestingTokenValidationFailedException()
    );

    verify(chain).doFilter(request, response);
  }

  @Nested
  class WithSubject {

    @Mock
    private Subject subject;

    @BeforeEach
    void setUpSubject() {
      ThreadContext.bind(subject);
    }

    @AfterEach
    void tearDownSubject() {
      ThreadContext.unbindSubject();
    }

    @Test
    void shouldContinueAsAnonymous() throws IOException, ServletException {
      configuration.setAnonymousMode(AnonymousMode.FULL);

      authenticationFilter.handleTokenValidationFailedException(
        request, response, chain, createLogoutTokenValidationFailedException()
      );

      verify(cookieIssuer).invalidate(request, response);
      verify(subject).login(any(AnonymousToken.class));
      verify(chain).doFilter(request, response);
    }
  }

  private TokenValidationFailedException createLogoutTokenValidationFailedException() {
    return new TokenValidationFailedException(validator, accessToken);
  }

  private TokenValidationFailedException createTestingTokenValidationFailedException() {
    return new TokenValidationFailedException(new TestingValidator(), accessToken);
  }

  private static class TestingValidator implements AccessTokenValidator {

    @Override
    public boolean validate(AccessToken token) {
      return false;
    }
  }

}
