package com.cloudogu.scm.cas.browser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenValidator;

import javax.inject.Inject;

@Extension
public class LogoutAccessTokenValidator implements AccessTokenValidator {

  private static final Logger LOG = LoggerFactory.getLogger(LogoutAccessTokenValidator.class);

  private final TicketStore ticketStore;

  @Inject
  public LogoutAccessTokenValidator(TicketStore ticketStore) {
    this.ticketStore = ticketStore;
  }

  @Override
  public boolean validate(AccessToken token) {
    LOG.trace("checking whether token {} with parent id {} is valid", token.getId(), token.getParentKey());
    String id = resolveId(token);
    return !ticketStore.isBlacklisted(id);
  }

  private String resolveId(AccessToken token) {
    return token.getParentKey().orElse(token.getId());
  }
}
