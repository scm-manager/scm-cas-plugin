package com.cloudogu.scm.cas.browser;

import sonia.scm.security.AccessToken;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class TicketStore {

  private static final String STORE_NAME = "cas";

  private final DataStore<StoreEntry> byCasTicket;
  private final Map<String, StoreEntry> byAccessTokenId = new ConcurrentHashMap<>();

  @Inject
  public TicketStore(DataStoreFactory storeFactory) {
    this.byCasTicket = storeFactory.withType(StoreEntry.class).withName(STORE_NAME).build();

    loadFromStore();
  }

  private void loadFromStore() {
    Map<String, StoreEntry> all = byCasTicket.getAll();
    Collection<StoreEntry> values = all.values();
    for (StoreEntry e : values) {
      byAccessTokenId.put(e.accessTokenId, e);
    }
  }

  public void login(CasToken casToken, AccessToken accessToken) {
    StoreEntry entry = create(accessToken);
    byCasTicket.put(casToken.getCredentials(), entry);
    byAccessTokenId.put(entry.getAccessTokenId(), entry);
  }

  public void logout(String casTicket) {
    StoreEntry entry = byCasTicket.get(casTicket);
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

  private StoreEntry create(AccessToken accessToken) {
    String id = accessToken.getParentKey().orElse(accessToken.getId());
    return new StoreEntry(id);
  }

  @XmlRootElement
  public static class StoreEntry {

    private String accessTokenId;
    private boolean blacklistet = false;

    public StoreEntry() {}

    public StoreEntry(String accessTokenId) {
      this.accessTokenId = accessTokenId;
    }

    public String getAccessTokenId() {
      return accessTokenId;
    }

    public boolean isBlacklistet() {
      return blacklistet;
    }

    public void setBlacklistet(boolean blacklistet) {
      this.blacklistet = blacklistet;
    }
  }

}
