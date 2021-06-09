package com.cloudogu.cas;

import org.jasig.cas.client.authentication.AttributePrincipal;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;

public class GreetingServlet extends HttpServlet {

  private final HttpClient client = HttpClient.newHttpClient();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try (PrintWriter writer = resp.getWriter()) {
      AttributePrincipal principal = (AttributePrincipal) req.getUserPrincipal();

      Map<String, Object> attributes = principal.getAttributes();
      for (Map.Entry<String, Object> entry : attributes.entrySet()) {
        writer.append(entry.getKey()).append(" = ").println(entry.getValue());
      }

      String proxyTicket = principal.getProxyTicketFor("http://scm.hitchhiker.com:8081/scm/api/v2/cas/auth/");
      writer.append("proxyTicket = ").println(proxyTicket);

      String accessToken = obtainAccessToken(proxyTicket);
      writer.append("accessToken = ").println(accessToken);

      String me = fetchMe(accessToken);
      writer.append("me = ").println(me);
    }
  }

  private String fetchMe(String accessToken) throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create("http://scm.hitchhiker.com:8081/scm/api/v2/me"))
      .header("Authorization", "Bearer " + accessToken)
      .GET()
      .build();

    try {
      HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
      return response.body().trim();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("failed to fetch me data", e);
    }
  }

  private String obtainAccessToken(String proxyTicket) throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create("http://scm.hitchhiker.com:8081/scm/api/v2/cas/auth/"))
      .header("Content-Type", "application/x-www-form-urlencoded")
      .POST(BodyPublishers.ofString("ticket=" + proxyTicket))
      .build();

    try {
      HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
      return response.body().trim();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("failed to obtain access token", e);
    }
  }
}
