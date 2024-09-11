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

package com.cloudogu.scm.cas.rest;

import com.cloudogu.scm.cas.CasContext;
import org.apache.shiro.authc.AuthenticationException;
import sonia.scm.SCMContextProvider;
import sonia.scm.Stage;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
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
