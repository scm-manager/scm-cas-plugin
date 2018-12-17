package com.cloudogu.scm.cas.browser;

import sonia.scm.security.AccessToken;

import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class TicketStore {

  private final Map<String, StoreEntry> byCasToken = new ConcurrentHashMap<>();
  private final Map<String, StoreEntry> byAccessTokenId = new ConcurrentHashMap<>();

  public void login(CasToken casToken, AccessToken accessToken) {
    StoreEntry entry = new StoreEntry(accessToken);
    byCasToken.put(casToken.getCredentials(), entry);
    byAccessTokenId.put(entry.getId(), entry);
  }

  public void logout(String casTicket) {
    StoreEntry entry = byCasToken.get(casTicket);
    if (entry != null) {
      entry.setBlacklistet(true);
    }
  }

  public boolean isBlacklistet(String accessTokenId) {
    StoreEntry entry = byAccessTokenId.get(accessTokenId);
    if (entry != null) {
      return entry.isBlacklistet();
    }
    return false;
  }

  private static class StoreEntry {

    private final AccessToken accessToken;
    private boolean blacklistet = false;

    public StoreEntry(AccessToken accessToken) {
      this.accessToken = accessToken;
    }

    public String getId() {
      return accessToken.getParentKey().orElse(accessToken.getId());
    }

    public boolean isBlacklistet() {
      return blacklistet;
    }

    public void setBlacklistet(boolean blacklistet) {
      this.blacklistet = blacklistet;
    }
  }

}
