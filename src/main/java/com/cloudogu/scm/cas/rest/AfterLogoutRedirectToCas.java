package com.cloudogu.scm.cas.rest;

import com.cloudogu.scm.cas.CasContext;
import com.cloudogu.scm.cas.Configuration;
import sonia.scm.api.v2.resources.LogoutRedirection;
import sonia.scm.plugin.Extension;

import javax.inject.Inject;
import java.net.URI;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Extension
public class AfterLogoutRedirectToCas implements LogoutRedirection {

  private final CasContext context;

  @Inject
  public AfterLogoutRedirectToCas(CasContext context) {
    this.context = context;
  }

  @Override
  public Optional<URI> afterLogoutRedirectTo() {
    Configuration configuration = context.get();
    if (configuration.isEnabled()) {
      String casBaseUrl = configuration.getCasUrl();
      if (!casBaseUrl.endsWith("/")) {
        casBaseUrl = casBaseUrl + "/";
      }
      return of(URI.create(casBaseUrl).resolve("./logout"));
    } else {
      return empty();
    }
  }
}
