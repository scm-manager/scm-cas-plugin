package com.cloudogu.scm.cas.browser;

import sonia.scm.plugin.Extension;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenValidator;

import javax.inject.Inject;

@Extension
public class LogoutAccessTokenValidator implements AccessTokenValidator {

  private final TicketStore ticketStore;

  @Inject
  public LogoutAccessTokenValidator(TicketStore ticketStore) {
    this.ticketStore = ticketStore;
  }

  @Override
  public boolean validate(AccessToken token) {
    String id = resolveId(token);
    return !ticketStore.isBlacklisted(id);
  }

  private String resolveId(AccessToken token) {
    return token.getParentKey().orElse(token.getId());
  }
}
