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

package com.cloudogu.scm.cas;

import com.cloudogu.scm.cas.browser.CasAuthenticationResource;
import com.cloudogu.scm.cas.browser.CasToken;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.CipherHandler;
import sonia.scm.util.HttpUtil;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ServiceUrlProvider {

  private static final Logger LOG = LoggerFactory.getLogger(ServiceUrlProvider.class);

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
    return createFromSuffix(request -> request.getRequestURI().substring(request.getContextPath().length()));
  }

  public String createRoot() {
    return createFromSuffix(request -> "/");
  }

  private String createFromSuffix(Function<HttpServletRequest, String> urlSuffixProvider) {
    Optional<HttpServletRequest> optionalRequest = requestHolder.getRequest();
    if (optionalRequest.isPresent()) {
      LOG.debug("create url from http request");
      return createUrlFromRequest(optionalRequest.get(), urlSuffixProvider.apply(optionalRequest.get()));
    }
    LOG.debug("http request not found, create url from configuration");
    return createUrlFromConfiguration();
  }

  private String createUrlFromConfiguration() {
    String encoded = cipherHandler.encode("/");

    StringBuilder url = new StringBuilder();
    url.append(scmConfiguration.getBaseUrl());
    url.append("/scm/api/");
    url.append(CasAuthenticationResource.PATH);
    if (encoded != null) {
      url.append("/");
      url.append(encoded);
    }

    return url.toString();
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

  private String createUrlFromRequest(HttpServletRequest request, String urlSuffix) {
    String urlSuffixWithParameters;
    if (urlSuffix.equals("/login")) {
      urlSuffixWithParameters = request.getParameter("from");
    } else {
      urlSuffixWithParameters = urlSuffix + buildQueryParameters(request);
    }
    String encoded = cipherHandler.encode(Strings.isNullOrEmpty(urlSuffixWithParameters) ? "/" : urlSuffixWithParameters);
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

  private static class SingleParameter {
    private final String key;
    private final String value;

    private SingleParameter(String key, String value) {
      this.key = key;
      this.value = value;
    }

    private String asQueryString() {
      return HttpUtil.encode(key) + "=" + HttpUtil.encode(value);
    }
  }
}
