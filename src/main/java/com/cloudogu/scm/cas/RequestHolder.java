package com.cloudogu.scm.cas;

import com.google.inject.Inject;
import com.google.inject.Provider;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class RequestHolder {

  private Provider<HttpServletRequest> requestProvider;

  @Inject
  RequestHolder(Provider<HttpServletRequest> requestProvider) {
    this.requestProvider = requestProvider;
  }

  public Optional<HttpServletRequest> getRequest() {
    return Optional.ofNullable(requestProvider.get());
  }
}
