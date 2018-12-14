package com.cloudogu.scm.cas;

import org.apache.shiro.authc.AuthenticationException;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CasRestClient {

  private final AdvancedHttpClient httpClient;
  private final Configuration configuration;

  @Inject
  public CasRestClient(AdvancedHttpClient httpClient, Configuration configuration) {
    this.httpClient = httpClient;
    this.configuration = configuration;
  }

  public String requestGrantingTicketUrl(String username, String password) {
    AdvancedHttpResponse response = execute(httpClient.post(configuration.getCasUrl() + "/v1/tickets")
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

  public String requestServiceTicket(String url, String serviceUrl) {
    AdvancedHttpResponse response = execute(httpClient.post(url)
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

  public AdvancedHttpResponse execute(AdvancedHttpRequestWithBody request) {
    try {
      return request.request();
    } catch (IOException ex) {
      throw new AuthenticationException("failed to execute cas rest request", ex);
    }
  }
}
