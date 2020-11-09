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
package com.cloudogu.scm.cas.rest;

import com.cloudogu.scm.cas.CasContext;
import com.cloudogu.scm.cas.Configuration;
import org.apache.shiro.authc.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sonia.scm.SCMContextProvider;
import sonia.scm.Stage;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.net.ahc.FormContentBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CasRestClientTest {

  private static final String CAS_URL = "https://cas.hitchhiker.com/cas";
  private static final String TGT_LOCATION = CAS_URL + "/v1/tickets/TGT-123";
  public static final String SERVICE_URL = "http://localhost:8081/scm";

  @Mock
  private CasContext context;

  private Configuration configuration;

  @Mock
  private SCMContextProvider contextProvider;

  @Mock
  private AdvancedHttpClient httpClient;

  private CasRestClient restClient;

  @BeforeEach
  void setUpObjectUnderTest() {
    configuration = new Configuration();
    configuration.setCasUrl(CAS_URL);

    when(context.get()).thenReturn(configuration);

    when(contextProvider.getStage()).thenReturn(Stage.PRODUCTION);

    restClient = new CasRestClient(contextProvider, httpClient, context);
  }

  @Test
  void shouldReturnGrantingTicket() throws IOException {
    String tgtLocation = TGT_LOCATION;

    AdvancedHttpResponse response = mockForTGT("trillian", "secret");
    when(response.getStatus()).thenReturn(HttpServletResponse.SC_CREATED);
    when(response.getFirstHeader("Location")).thenReturn(tgtLocation);

    String uri = restClient.requestGrantingTicketUrl("trillian", "secret");
    assertThat(uri).isEqualTo(tgtLocation);
  }

  @Test
  void shouldThrowAnAuthenticationExceptionForRequestTGT() throws IOException {
    AdvancedHttpResponse response = mockForTGT("trillian", "wrongpwd");
    when(response.getStatus()).thenReturn(HttpServletResponse.SC_BAD_REQUEST);

    assertThrows(AuthenticationException.class, () -> restClient.requestGrantingTicketUrl("trillian", "wrongpwd"));
  }

  @Test
  void shouldWrapIOExceptionForRequestTGT() throws IOException {
    AdvancedHttpRequestWithBody request = mockRequestForTGT("trillian", "secret");
    when(request.request()).thenThrow(IOException.class);

    assertThrows(AuthenticationException.class, () -> restClient.requestGrantingTicketUrl("trillian", "secret"));
  }

  @Test
  void shouldReturnServiceTicket() throws IOException {
    AdvancedHttpResponse response = mockForST(TGT_LOCATION, SERVICE_URL);
    when(response.getStatus()).thenReturn(HttpServletResponse.SC_OK);
    when(response.contentAsString()).thenReturn("ST-456");

    String serviceTicket = restClient.requestServiceTicket(TGT_LOCATION, SERVICE_URL);
    assertThat(serviceTicket).isEqualTo("ST-456");
  }

  @Test
  void shouldThrowAnAuthenticationExceptionForRequestST() throws IOException {
    AdvancedHttpResponse response = mockForST(TGT_LOCATION, SERVICE_URL);
    when(response.getStatus()).thenReturn(HttpServletResponse.SC_BAD_REQUEST);

    assertThrows(AuthenticationException.class, () -> restClient.requestServiceTicket(TGT_LOCATION, SERVICE_URL));
  }

  @Test
  void shouldWrapIOExceptionForRequestST() throws IOException {
    AdvancedHttpRequestWithBody request = mockRequestForST(TGT_LOCATION, SERVICE_URL);
    when(request.request()).thenThrow(IOException.class);

    assertThrows(AuthenticationException.class, () -> restClient.requestServiceTicket(TGT_LOCATION, SERVICE_URL));
  }

  @Test
  void shouldWrapIOExceptionForSTContent() throws IOException {
    AdvancedHttpResponse response = mockForST(TGT_LOCATION, SERVICE_URL);
    when(response.getStatus()).thenReturn(HttpServletResponse.SC_OK);
    when(response.contentAsString()).thenThrow(IOException.class);

    assertThrows(AuthenticationException.class, () -> restClient.requestServiceTicket(TGT_LOCATION, SERVICE_URL));
  }

  private AdvancedHttpResponse mockForST(String tgtUrl, String serviceUrl) throws IOException {
    AdvancedHttpRequestWithBody request = mockRequestForST(tgtUrl, serviceUrl);

    AdvancedHttpResponse response = mock(AdvancedHttpResponse.class);
    when(request.request()).thenReturn(response);
    return response;
  }

  private AdvancedHttpRequestWithBody mockRequestForST(String tgtUrl, String serviceUrl) {
    AdvancedHttpRequestWithBody request = mockRequest();
    when(httpClient.post(tgtUrl)).thenReturn(request);

    FormContentBuilder formContentBuilder = mock(FormContentBuilder.class);
    when(request.formContent()).thenReturn(formContentBuilder);

    when(formContentBuilder.field("service", serviceUrl)).thenReturn(formContentBuilder);
    when(formContentBuilder.build()).thenReturn(request);
    return request;
  }

  private AdvancedHttpResponse mockForTGT(String username, String password) throws IOException {
    AdvancedHttpRequestWithBody request = mockRequestForTGT(username, password);

    AdvancedHttpResponse response = mock(AdvancedHttpResponse.class);
    when(request.request()).thenReturn(response);
    return response;
  }

  private AdvancedHttpRequestWithBody mockRequestForTGT(String username, String password) {
    AdvancedHttpRequestWithBody request = mockRequest();
    when(httpClient.post(CAS_URL + "/v1/tickets")).thenReturn(request);

    FormContentBuilder formContentBuilder = mock(FormContentBuilder.class);
    when(request.formContent()).thenReturn(formContentBuilder);

    when(formContentBuilder.field("username", username)).thenReturn(formContentBuilder);
    when(formContentBuilder.field("password", password)).thenReturn(formContentBuilder);
    when(formContentBuilder.build()).thenReturn(request);

    return request;
  }

  private AdvancedHttpRequestWithBody mockRequest() {
    AdvancedHttpRequestWithBody request = mock(AdvancedHttpRequestWithBody.class, RETURNS_SELF);
    when(request.disableCertificateValidation(false)).thenReturn(request);
    return request;
  }

}
