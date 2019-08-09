package com.cloudogu.scm.cas;

import com.google.inject.Inject;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public class RequestHolder {

  @Inject(optional = true)
  private HttpServletRequest request;

  public Optional<HttpServletRequest> getRequest() {
      return Optional.ofNullable(request);
  }
}
