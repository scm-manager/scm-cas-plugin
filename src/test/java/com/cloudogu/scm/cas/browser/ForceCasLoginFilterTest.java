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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

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

  private ForceCasLoginFilter filter;

  private ThreadState subjectThreadState;

  @BeforeEach
  void setUpObjectUnderTest() {
    Configuration configuration = new Configuration();
    configuration.setCasUrl(CAS_URL);

    when(casContext.get()).thenReturn(configuration);

    filter = new ForceCasLoginFilter(serviceUrlProvider, casContext);

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
  void shouldNotRedirectOnCasLogin() throws IOException, ServletException {
    when(request.getContextPath()).thenReturn("/scm");
    when(request.getRequestURI()).thenReturn("/scm/api/v2/cas/__enc__");
    when(request.getMethod()).thenReturn("GET");
    when(request.getParameter("ticket")).thenReturn("TGT-123");

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
  }

  @Test
  void shouldNotRedirectOnCasLogout() throws IOException, ServletException {
    when(request.getContextPath()).thenReturn("/scm");
    when(request.getRequestURI()).thenReturn("/scm/api/v2/cas/__enc__");
    when(request.getMethod()).thenReturn("POST");
    when(request.getContentType()).thenReturn(MediaType.APPLICATION_FORM_URLENCODED);

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
