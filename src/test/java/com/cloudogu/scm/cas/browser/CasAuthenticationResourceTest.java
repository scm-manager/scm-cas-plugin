package com.cloudogu.scm.cas.browser;

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
import sonia.scm.security.CipherHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CasAuthenticationResourceTest {

  @Mock
  private AccessTokenBuilderFactory tokenBuilderFactory;

  @Mock
  private AccessTokenBuilder tokenBuilder;

  @Mock
  private AccessToken token;

  @Mock
  private AccessTokenCookieIssuer cookieIssuer;

  @Mock
  private CipherHandler cipherHandler;

  @InjectMocks
  private CasAuthenticationResource casAuthenticationResource;

  @Mock
  private HttpServletRequest servletRequest;

  @Mock
  private HttpServletResponse servletResponse;

  @Mock
  private Subject subject;

  private SubjectThreadState subjectThreadState;

  @BeforeEach
  void setUpMocks() {
    when(servletRequest.getContextPath()).thenReturn("/scm");
    when(servletRequest.getRequestURL()).thenReturn(new StringBuffer("http://hitchhiker.com/scm/repos"));
    when(servletRequest.getRequestURI()).thenReturn("/scm/repos");


    subjectThreadState = new SubjectThreadState(subject);
    subjectThreadState.bind();
  }

  @AfterEach
  void tearDown() {
    subjectThreadState.clear();
  }

  @Test
  void shouldAuthenticateAndRedirect() {
    when(cipherHandler.decode("__enc__")).thenReturn("/repos");
    when(tokenBuilderFactory.create()).thenReturn(tokenBuilder);
    when(tokenBuilder.build()).thenReturn(token);

    Response response = casAuthenticationResource.authenticate(
      servletRequest,
      servletResponse,
      "__enc__",
      "TGT-123"
    );

    verify(subject).login(any(CasToken.class));
    verify(cookieIssuer).authenticate(servletRequest, servletResponse, token);

    assertThat(response.getLocation()).isEqualTo(URI.create("http://hitchhiker.com/scm/repos"));

  }

}
