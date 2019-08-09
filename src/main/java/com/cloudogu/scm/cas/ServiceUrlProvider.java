package com.cloudogu.scm.cas;

import com.cloudogu.scm.cas.browser.CasAuthenticationResource;
import com.cloudogu.scm.cas.browser.CasToken;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.CipherHandler;
import sonia.scm.util.HttpUtil;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class ServiceUrlProvider {

  private final RequestHolder requestHolder;
  private final CipherHandler cipherHandler;
  private final ScmConfiguration scmConfiguration;

  @Inject
  public ServiceUrlProvider(RequestHolder requestHolder, CipherHandler cipherHandler, ScmConfiguration scmConfiguration) {
    this.requestHolder = requestHolder;
    this.cipherHandler = cipherHandler;
    this.scmConfiguration = scmConfiguration;
  }

  public String create() {
    Optional<HttpServletRequest> optionalRequest = requestHolder.getRequest();
    if (optionalRequest.isPresent()) {
      return createUrlFromRequest(optionalRequest.get());
    }
    return createUrlFromConfiguration();
  }

  private String createUrlFromConfiguration() {
    StringBuilder url = new StringBuilder();
    url.append(scmConfiguration.getBaseUrl());
    url.append("/api/" + CasAuthenticationResource.PATH);
//    url.append("/suffix?");

    return url.toString();
  }

  private String createUrlFromRequest(HttpServletRequest request) {
    String urlSuffix = request.getRequestURI().substring(request.getContextPath().length());
    String encoded = cipherHandler.encode(urlSuffix);
    return HttpUtil.getCompleteUrl(request, "api", CasAuthenticationResource.PATH, encoded);
  }

  public String createFromToken(CasToken casToken) {
    Optional<HttpServletRequest> optionalRequest = requestHolder.getRequest();
    if (optionalRequest.isPresent()) {
      return createUrl(optionalRequest.get(), casToken.getUrlSuffix());
    }
    throw new IllegalStateException("request scope is not available");
  }

  private String createUrl(HttpServletRequest request, String suffix) {
    return HttpUtil.getCompleteUrl(request, "api", CasAuthenticationResource.PATH, suffix);
  }
}
