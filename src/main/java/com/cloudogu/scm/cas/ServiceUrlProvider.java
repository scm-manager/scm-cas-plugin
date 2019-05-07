package com.cloudogu.scm.cas;

import com.cloudogu.scm.cas.browser.CasAuthenticationResource;
import com.cloudogu.scm.cas.browser.CasToken;
import sonia.scm.security.CipherHandler;
import sonia.scm.util.HttpUtil;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

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
    String urlSuffixWithParameters = urlSuffix + buildQueryParameters(request);
    String encoded = cipherHandler.encode(urlSuffixWithParameters);
    return HttpUtil.getCompleteUrl(request, "api", CasAuthenticationResource.PATH, encoded);
  }

  private String buildQueryParameters(HttpServletRequest request) {
    Map<String, String[]> parameterMap = request.getParameterMap();
    if (parameterMap.isEmpty()) {
      return "";
    }
    return parameterMap
      .entrySet()
      .stream()
      .flatMap(e -> Arrays.stream(e.getValue()).map(parameter -> new SingleParameter(e.getKey(), parameter)))
      .map(SingleParameter::asQueryString)
      .collect(Collectors.joining("&", "?", ""));
  }

  public String createFromToken(CasToken casToken) {
    return createUrl(requestProvider.get(), casToken.getUrlSuffix());
  }

  private String createUrl(HttpServletRequest request, String suffix) {
    return HttpUtil.getCompleteUrl(request, "api", CasAuthenticationResource.PATH, suffix);
  }

  private static class SingleParameter {
    private final String key;
    private final String value;

    public SingleParameter(String key, String value) {
      this.key = key;
      this.value = value;
    }

    private String asQueryString() {
      return HttpUtil.encode(key) + "=" + HttpUtil.encode(value);
    }
  }
}
