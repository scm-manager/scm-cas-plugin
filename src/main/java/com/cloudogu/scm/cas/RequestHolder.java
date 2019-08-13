package com.cloudogu.scm.cas;

import com.google.inject.Inject;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class RequestHolder {

  private Provider<HttpServletRequest> requestProvider;

  @Inject
  public RequestHolder(Provider<HttpServletRequest> requestProvider) {
    this.requestProvider = requestProvider;
  }

  public Optional<HttpServletRequest> getRequest() {
    try {
      return Optional.of(requestProvider.get());
    } catch (ProvisionException ex) {
      if (ex.getCause() instanceof OutOfScopeException) {
        return Optional.empty();
      } else {
        throw ex;
      }
    }
  }
}
