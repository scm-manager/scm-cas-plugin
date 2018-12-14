package com.cloudogu.scm.cas;

import com.google.common.base.Strings;
import org.apache.shiro.authc.AuthenticationToken;

import static com.google.common.base.Preconditions.checkArgument;

public final class CasToken implements AuthenticationToken {

  private final String ticket;
  private final String urlSuffix;

  private CasToken(String ticket, String urlSuffix) {
    this.ticket = ticket;
    this.urlSuffix = urlSuffix;
  }

  @Override
  public String getCredentials() {
    return ticket;
  }

  public String getUrlSuffix() {
    return urlSuffix;
  }

  @Override
  public Object getPrincipal() {
    throw new UnsupportedOperationException("CasToken has principal, it provides only credentials");
  }

  public static CasToken valueOf(String ticket, String urlSuffix) {
    checkArgument(!Strings.isNullOrEmpty(ticket), "ticket is null or empty");
    checkArgument(!Strings.isNullOrEmpty(urlSuffix), "urlSuffix is null or empty");
    return new CasToken(ticket, urlSuffix);
  }
}
