package com.cloudogu.scm.cas.browser;

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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForceCasLoginFilterTest {

  private static final String CAS_LOGIN_URL = "https://sso.hitchhiker.com/login";
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

  private ForceCasLoginFilter filter;

  private ThreadState subjectThreadState;

  @BeforeEach
  void setUpObjectUnderTest() {
    Configuration configuration = new Configuration();
    configuration.setCasLoginUrl(CAS_LOGIN_URL);

    filter = new ForceCasLoginFilter(serviceUrlProvider, configuration);

    subjectThreadState = new SubjectThreadState(subject);
    subjectThreadState.bind();
  }

  @AfterEach
  void cleanUpAuthenticationEnvironment() {
    subjectThreadState.clear();
  }

  @Test
  void shouldRedirectToCas() throws IOException, ServletException {
    when(request.getRequestURI()).thenReturn("/scm/repos");
    when(serviceUrlProvider.create()).thenReturn(SERVICE_URL);

    filter.doFilter(request, response, chain);

    verify(response).sendRedirect(CAS_LOGIN_URL + "?service=" + SERVICE_URL_ESCAPED);
  }

  @Test
  void shouldNotRedirectOnCasCallback() throws IOException, ServletException {
    when(request.getRequestURI()).thenReturn("/scm/api/v2/cas/__enc__");
    when(request.getParameter("ticket")).thenReturn("TGT-123");

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
  }

  @Test
  void shouldNotRedirectIfUserIsAuthenticated() throws IOException, ServletException {
    when(subject.isAuthenticated()).thenReturn(true);

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
  }

}
