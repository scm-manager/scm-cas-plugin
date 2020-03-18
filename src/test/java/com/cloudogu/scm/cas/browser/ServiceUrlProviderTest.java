/**
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
import java.util.Collections;
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

    @Test
    void shouldIncludeParameters() {
      when(request.getParameterMap()).thenReturn(Collections.singletonMap("create", new String[]{"true"}));
      when(cipherHandler.encode("/repos?create=true")).thenReturn("__repos__");
      String serviceUrl = resolver.create();
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
