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
import org.apache.shiro.authc.AuthenticationException;
import sonia.scm.SCMContextProvider;
import sonia.scm.Stage;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CasRestClient {

  private final SCMContextProvider contextProvider;
  private final AdvancedHttpClient httpClient;
  private final CasContext context;

  @Inject
  public CasRestClient(SCMContextProvider contextProvider, AdvancedHttpClient httpClient, CasContext context) {
    this.contextProvider = contextProvider;
    this.httpClient = httpClient;
    this.context = context;
  }

  public String requestGrantingTicketUrl(String username, String password) {
    AdvancedHttpResponse response = execute(post(context.get().getCasUrl() + "/v1/tickets")
      .disableCertificateValidation(isDevelopmentStageActive())
      .formContent()
      .field("username", username)
      .field("password", password)
      .build());

    int statusCode = response.getStatus();
    if (statusCode != HttpServletResponse.SC_CREATED) {
      throw new AuthenticationException("failed to request granting ticket, server returned status " + statusCode);
    }

    return response.getFirstHeader("Location");
  }

  private boolean isDevelopmentStageActive() {
    return contextProvider.getStage() == Stage.DEVELOPMENT;
  }

  public String requestServiceTicket(String url, String serviceUrl) {
    AdvancedHttpResponse response = execute(post(url)
      .formContent()
      .field("service", serviceUrl)
      .build());

    int statusCode = response.getStatus();
    if (statusCode != HttpServletResponse.SC_OK) {
      throw new AuthenticationException("failed to request service ticket, server returned status " + statusCode);
    }

    try {
      return response.contentAsString();
    } catch (IOException ex) {
      throw new AuthenticationException("failed to read service ticket from response", ex);
    }
  }

  private AdvancedHttpRequestWithBody post(String url) {
    return httpClient.post(url)
      .spanKind("CAS")
      .acceptStatusCodes(400, 401, 403) // status code 400 is used for failed logins by cas, too
      .disableCertificateValidation(isDevelopmentStageActive());
  }

  private AdvancedHttpResponse execute(AdvancedHttpRequestWithBody request) {
    try {
      return request.request();
    } catch (IOException ex) {
      throw new AuthenticationException("failed to execute cas rest request", ex);
    }
  }
}
