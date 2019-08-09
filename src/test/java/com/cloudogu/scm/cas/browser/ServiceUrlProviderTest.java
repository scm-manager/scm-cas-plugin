package com.cloudogu.scm.cas.browser;

import com.cloudogu.scm.cas.RequestHolder;
import com.cloudogu.scm.cas.ServiceUrlProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.CipherHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServiceUrlProviderTest {

  @Mock
  private RequestHolder requestHolder;

  @Mock
  private Optional<HttpServletRequest> optionalRequest;

  @Mock
  private HttpServletRequest request;

  @Mock
  private ScmConfiguration scmConfiguration;

  @Mock
  private CipherHandler cipherHandler;

  private ServiceUrlProvider resolver;

  @BeforeEach
  void initRequestHolderAndResolver() {
    when(requestHolder.getRequest()).thenReturn(optionalRequest);
    resolver = new ServiceUrlProvider(requestHolder, cipherHandler, scmConfiguration);
  }

  @Nested
  class withHTTPRequest {

    @BeforeEach
    void setUpObjectUnderTest() {
      when(optionalRequest.isPresent()).thenReturn(true);
      when(optionalRequest.get()).thenReturn(request);
      when(request.getContextPath()).thenReturn("/scm");
      when(request.getRequestURI()).thenReturn("/scm/repos");
      when(request.getRequestURL()).thenReturn(new StringBuffer("https://hitchhiker.com/scm/repos"));
    }

    @Test
    void shouldCreateUri() {
      when(cipherHandler.encode("/repos")).thenReturn("__repos__");

      String serviceUrl = resolver.create();
      assertThat(serviceUrl).isEqualTo("https://hitchhiker.com/scm/api/v2/cas/auth/__repos__");
    }

    @Test
    void shouldCreateUriFromToken() {
      String serviceUrl = resolver.createFromToken(CasToken.valueOf("TGT-123", "__repos__"));
      assertThat(serviceUrl).isEqualTo("https://hitchhiker.com/scm/api/v2/cas/auth/__repos__");
    }
  }

  @Nested
  class withoutHTTPRequest {

    @BeforeEach
    void mockConfiguration() {
      when(scmConfiguration.getBaseUrl()).thenReturn("https://hitchhiker.com");
      when(optionalRequest.isPresent()).thenReturn(false);
    }

    @Test
    void shouldCreateUrlFromConfiguration() {
      String serviceUrl = resolver.create();
      assertThat(serviceUrl).isEqualTo("https://hitchhiker.com/scm/api/v2/cas/auth");
    }

    @Test
    void shouldThrowIllegalStateException() {
      assertThrows(IllegalStateException.class, () -> resolver.createFromToken(CasToken.valueOf("TGT-123", "__repos__")));
    }
  }
}
