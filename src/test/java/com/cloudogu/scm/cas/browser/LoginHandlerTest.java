/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.cloudogu.scm.cas.browser;

import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilder;
import sonia.scm.security.AccessTokenBuilderFactory;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.user.UserTestData;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginHandlerTest {

  @Mock
  private AccessTokenBuilderFactory tokenBuilderFactory;

  @Mock
  private AccessTokenBuilder tokenBuilder;

  @Mock
  private AccessToken token;

  @Mock
  private TicketStore ticketStore;

  @Mock
  private AccessTokenCookieIssuer cookieIssuer;

  @Mock
  private Subject subject;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  private SubjectThreadState subjectThreadState;

  @InjectMocks
  private LoginHandler loginHandler;

  @BeforeEach
  void setUpMocks() {
    subjectThreadState = new SubjectThreadState(subject);
    subjectThreadState.bind();
  }

  @AfterEach
  void tearDown() {
    subjectThreadState.clear();
  }

  @Test
  void shouldLogin() {
    SimplePrincipalCollection principals = new SimplePrincipalCollection();
    principals.add("trillian", "h2g2");
    principals.add(UserTestData.createTrillian(), "h2g2");
    when(subject.getPrincipals()).thenReturn(principals);

    when(tokenBuilderFactory.create()).thenReturn(tokenBuilder);
    when(tokenBuilder.build()).thenReturn(token);

    CasToken casToken = CasToken.valueOf("ST-123", "__enc__");

    loginHandler.login(request, response, casToken);

    verify(subject).login(any(CasToken.class));
    verify(ticketStore).login(casToken, token);
    verify(cookieIssuer).authenticate(request, response, token);
  }

}
