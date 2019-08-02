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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
