package com.cloudogu.scm.cas.browser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.security.AccessToken;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.xml.XmlInstantAdapter;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class TicketStore {

  private static final Logger LOG = LoggerFactory.getLogger(TicketStore.class);

  private static final String STORE_NAME = "cas";

  private final DataStore<StoreEntry> byCasTicket;
  private final Clock clock;

  private final Map<String, StoreEntry> byAccessTokenId = new ConcurrentHashMap<>();

  @Inject
  public TicketStore(DataStoreFactory storeFactory) {
    this(storeFactory, Clock.systemUTC());
  }

  public TicketStore(DataStoreFactory storeFactory, Clock clock) {
    this.byCasTicket = storeFactory.withType(StoreEntry.class).withName(STORE_NAME).build();
    this.clock = clock;

    loadFromStore();
  }

  private void loadFromStore() {
    for (StoreEntry e : byCasTicket.getAll().values()) {
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
      byCasTicket.put(casTicket, entry);
      byAccessTokenId.put(entry.getAccessTokenId(), entry);
    }
  }

  public boolean isBlacklisted(String accessTokenId) {
    StoreEntry entry = byAccessTokenId.get(accessTokenId);
    if (entry != null) {
      return entry.isBlacklistet();
    }
    return false;
  }

  public void removeExpired() {
    Set<String> expiredTokens = new HashSet<>();
    byCasTicket.getAll().forEach((casTicket, storeEntry) -> {
      if (isExpired(storeEntry)) {
        expiredTokens.add(casTicket);
      }
    });

    for (String casTicket : expiredTokens) {
      LOG.debug("remove expired ticket {}", casTicket);
      StoreEntry storeEntry = byCasTicket.get(casTicket);
      byAccessTokenId.remove(storeEntry.getAccessTokenId());
      byCasTicket.remove(casTicket);
    }
  }

  private boolean isExpired(StoreEntry storeEntry) {
    Instant instant = clock.instant();
    return storeEntry.expires.isBefore(instant);
  }

  private StoreEntry create(AccessToken accessToken) {
    String id = accessToken.getParentKey().orElse(accessToken.getId());
    Instant expires = computeExpiration(accessToken);
    return new StoreEntry(id, expires);
  }

  private Instant computeExpiration(AccessToken accessToken) {
    Instant expires = accessToken.getExpiration().toInstant();
    Optional<Date> refreshExpiration = accessToken.getRefreshExpiration();
    if (refreshExpiration.isPresent()) {
      Instant issuedAt = accessToken.getIssuedAt().toInstant();
      Duration duration = Duration.between(issuedAt, expires);
      expires = refreshExpiration.get().toInstant().plus(duration);
    }
    return expires;
  }

  @XmlRootElement(name = "cas-token")
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class StoreEntry {

    private String accessTokenId;
    @XmlJavaTypeAdapter(XmlInstantAdapter.class)
    private Instant expires;
    private boolean blacklistet = false;

    public StoreEntry() {}

    public StoreEntry(String accessTokenId, Instant expires) {
      this.accessTokenId = accessTokenId;
      this.expires = expires;
    }

    public String getAccessTokenId() {
      return accessTokenId;
    }

    public Instant getExpires() {
      return expires;
    }

    public boolean isBlacklistet() {
      return blacklistet;
    }

    public void setBlacklistet(boolean blacklistet) {
      this.blacklistet = blacklistet;
    }
  }

}
