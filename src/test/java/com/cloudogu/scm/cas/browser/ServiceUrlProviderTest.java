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

import jakarta.servlet.http.HttpServletRequest;
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

  @Test
  public void withLoginHTTPRequestWithFrom() {
    when(optionalRequest.isPresent()).thenReturn(true);
    when(optionalRequest.get()).thenReturn(request);
    when(request.getContextPath()).thenReturn("/scm");
    when(request.getRequestURI()).thenReturn("/scm/login");
    when(request.getRequestURL()).thenReturn(new StringBuffer("https://hitchhiker.com/scm/login"));
    when(request.getParameter("from")).thenReturn("/admin/plugins/installed");
    when(cipherHandler.encode("/admin/plugins/installed")).thenReturn("__plugins__");

    String serviceUrl = resolver.create();
    assertThat(serviceUrl).isEqualTo("https://hitchhiker.com/scm/api/v2/cas/auth/__plugins__");
  }

  @Test
  public void withLoginHTTPRequestWithoutFrom() {
    when(optionalRequest.isPresent()).thenReturn(true);
    when(optionalRequest.get()).thenReturn(request);
    when(request.getContextPath()).thenReturn("/scm");
    when(request.getRequestURI()).thenReturn("/scm/login");
    when(request.getRequestURL()).thenReturn(new StringBuffer("https://hitchhiker.com/scm/login"));
    when(cipherHandler.encode("/")).thenReturn("%2F");

    String serviceUrl = resolver.create();
    assertThat(serviceUrl).isEqualTo("https://hitchhiker.com/scm/api/v2/cas/auth/%2F");
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
    void shouldCreateRootUriFromRequest() {
      when(cipherHandler.encode("/")).thenReturn("%2F");

      String serviceUrl = resolver.createRoot();
      assertThat(serviceUrl).isEqualTo("https://hitchhiker.com/scm/api/v2/cas/auth/%2F");
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
    void shouldCreateUrlForRoot() {
      when(cipherHandler.encode("/")).thenReturn("%2F");
      String serviceUrl = resolver.createRoot();
      assertThat(serviceUrl).isEqualTo("https://hitchhiker.com/scm/api/v2/cas/auth/%2F");
    }

    @Test
    void shouldThrowIllegalStateException() {
      assertThrows(IllegalStateException.class, () -> resolver.createFromToken(CasToken.valueOf("TGT-123", "__repos__")));
    }
  }
}
