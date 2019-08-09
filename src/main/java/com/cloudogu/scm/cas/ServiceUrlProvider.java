package com.cloudogu.scm.cas;

import com.cloudogu.scm.cas.browser.CasAuthenticationResource;
import com.cloudogu.scm.cas.browser.CasToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.CipherHandler;
import sonia.scm.util.HttpUtil;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class ServiceUrlProvider {

  Logger LOG = LoggerFactory.getLogger(ServiceUrlProvider.class);

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
      LOG.debug("get url from request");
      return createUrlFromRequest(optionalRequest.get());
    }
    LOG.debug("get url from configuration");
    return createUrlFromConfiguration();
  }

  private String createUrlFromConfiguration() {
    StringBuilder url = new StringBuilder();
    url.append(scmConfiguration.getBaseUrl());
    url.append("/scm/api/" + CasAuthenticationResource.PATH);

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
