package com.cloudogu.scm.cas;

import com.cloudogu.scm.cas.browser.CasAuthenticationResource;
import com.cloudogu.scm.cas.browser.CasToken;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
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

  private final Provider<RequestHolder> requestHolder;
  private final CipherHandler cipherHandler;
  private final ScmConfiguration scmConfiguration;

  @Inject
  public ServiceUrlProvider(Provider<RequestHolder> requestHolder, CipherHandler cipherHandler, ScmConfiguration scmConfiguration) {
    this.requestHolder = requestHolder;
    this.cipherHandler = cipherHandler;
    this.scmConfiguration = scmConfiguration;
  }

  public String create() {
    Optional<HttpServletRequest> optionalRequest = getOptionalRequest();
    if (optionalRequest.isPresent()) {
      LOG.debug("create url from http request");
      return createUrlFromRequest(optionalRequest.get());
    }
    LOG.debug("http request not found");
    LOG.debug("create url from configuration");
    return createUrlFromConfiguration();
  }

  private Optional<HttpServletRequest> getOptionalRequest() {
    LOG.debug("trying to get http request");
    try {
      return requestHolder.get().getRequest();
    } catch (ProvisionException ex) {
      if (ex.getCause() instanceof OutOfScopeException) {
        return Optional.empty();
      } else {
        throw ex;
      }
    }
  }

  private String createUrlFromConfiguration() {
    StringBuilder url = new StringBuilder();
    url.append(scmConfiguration.getBaseUrl());
    url.append("/scm/api/");
    url.append(CasAuthenticationResource.PATH);

    return url.toString();
  }

  private String createUrlFromRequest(HttpServletRequest request) {
    String urlSuffix = request.getRequestURI().substring(request.getContextPath().length());
    String encoded = cipherHandler.encode(urlSuffix);
    return HttpUtil.getCompleteUrl(request, "api", CasAuthenticationResource.PATH, encoded);
  }

  public String createFromToken(CasToken casToken) {
    Optional<HttpServletRequest> optionalRequest = requestHolder.get().getRequest();
    if (optionalRequest.isPresent()) {
      return createUrl(optionalRequest.get(), casToken.getUrlSuffix());
    }
    throw new IllegalStateException("request scope is not available");
  }

  private String createUrl(HttpServletRequest request, String suffix) {
    return HttpUtil.getCompleteUrl(request, "api", CasAuthenticationResource.PATH, suffix);
  }
}
