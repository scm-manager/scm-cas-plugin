package com.cloudogu.scm.cas.browser;

import sonia.scm.plugin.Extension;
import sonia.scm.security.TokenClaimsValidator;

import javax.inject.Inject;
import java.util.Map;

@Extension
public class LogoutAccessTokenValidator implements TokenClaimsValidator {

  private final TicketStore ticketStore;

  @Inject
  public LogoutAccessTokenValidator(TicketStore ticketStore) {
    this.ticketStore = ticketStore;
  }

  @Override
  public boolean validate(Map<String, Object> claims) {
    String id = resolveId(claims);
    return !ticketStore.isBlacklistet(id);
  }

  private String resolveId(Map<String, Object> claims) {
    Object accessTokenId = claims.get("scm-manager.parentTokenId");
    if (accessTokenId == null) {
      accessTokenId = claims.get("jti");
    }
    return accessTokenId.toString();
  }
}
