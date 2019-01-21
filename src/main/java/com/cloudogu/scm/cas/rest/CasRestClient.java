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
