package com.cloudogu.scm.cas.browser;

import com.cloudogu.scm.cas.ServiceUrlProvider;
import com.google.inject.util.Providers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sonia.scm.security.CipherHandler;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServiceUrlProviderTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private CipherHandler cipherHandler;

  private ServiceUrlProvider resolver;

  @BeforeEach
  void setUpObjectUnderTest() {
    when(request.getContextPath()).thenReturn("/scm");
    when(request.getRequestURI()).thenReturn("/scm/repos");
    when(request.getRequestURL()).thenReturn(new StringBuffer("https://hitchhiker.com/scm/repos"));

    resolver = new ServiceUrlProvider(Providers.of(request), cipherHandler);
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
