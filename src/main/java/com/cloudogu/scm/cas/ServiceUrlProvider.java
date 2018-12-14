package com.cloudogu.scm.cas;

import com.cloudogu.scm.cas.browser.CasAuthenticationResource;
import com.cloudogu.scm.cas.browser.CasToken;
import sonia.scm.security.CipherHandler;
import sonia.scm.util.HttpUtil;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

public class ServiceUrlProvider {

  private final Provider<HttpServletRequest> requestProvider;
  private final CipherHandler cipherHandler;

  @Inject
  public ServiceUrlProvider(Provider<HttpServletRequest> requestProvider, CipherHandler cipherHandler) {
    this.requestProvider = requestProvider;
    this.cipherHandler = cipherHandler;
  }

  public String create() {
    HttpServletRequest request = requestProvider.get();
    String urlSuffix = request.getRequestURI().substring(request.getContextPath().length());
    String encoded = cipherHandler.encode(urlSuffix);
    return HttpUtil.getCompleteUrl(request, "api", CasAuthenticationResource.PATH, encoded);
  }

  public String createFromToken(CasToken casToken) {
    return createUrl(requestProvider.get(), casToken.getUrlSuffix());
  }

  private String createUrl(HttpServletRequest request, String suffix) {
    return HttpUtil.getCompleteUrl(request, "api", CasAuthenticationResource.PATH, suffix);
  }
}
