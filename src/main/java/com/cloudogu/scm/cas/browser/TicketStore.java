/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.scm.cas.browser;

import com.cloudogu.scm.cas.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.security.AccessToken;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.xml.XmlInstantAdapter;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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

  private final DataStore<StoreEntry> byCasTicket;
  private final Clock clock;

  private final Map<String, StoreEntry> byAccessTokenId = new ConcurrentHashMap<>();

  @Inject
  public TicketStore(DataStoreFactory storeFactory) {
    this(storeFactory, Clock.systemUTC());
  }

  public TicketStore(DataStoreFactory storeFactory, Clock clock) {
    this.byCasTicket = storeFactory.withType(StoreEntry.class).withName(Constants.NAME).build();
    this.clock = clock;

    loadFromStore();
  }

  private void loadFromStore() {
    for (StoreEntry e : byCasTicket.getAll().values()) {
      LOG.debug("restoring entry for access token id {} (blacklisted: {})", e.getAccessTokenId(), e.isBlacklisted());
      byAccessTokenId.put(e.accessTokenId, e);
    }
  }

  public void login(CasToken casToken, AccessToken accessToken) {
    StoreEntry entry = create(accessToken);
    byCasTicket.put(casToken.getCredentials(), entry);
    byAccessTokenId.put(entry.getAccessTokenId(), entry);
    LOG.trace("login for cas ticket {} with access token id {}", casToken.getCredentials(), entry.getAccessTokenId());
  }

  public Optional<String> logout(String casTicket) {
    StoreEntry entry = byCasTicket.get(casTicket);
    if (entry != null) {
      LOG.trace("blacklisting cas ticket {} with access token id {}", casTicket, entry.getAccessTokenId());
      entry.setBlacklisted(true);
      byCasTicket.put(casTicket, entry);
      byAccessTokenId.put(entry.getAccessTokenId(), entry);
      // old store entries may have no subject
      return Optional.ofNullable(entry.getSubject());
    } else {
      LOG.warn("no stored login entry found for cas ticket {}", casTicket);
    }
    return Optional.empty();
  }

  public boolean isBlacklisted(String accessTokenId) {
    StoreEntry entry = byAccessTokenId.get(accessTokenId);
    if (entry != null) {
      LOG.trace("found entry with id {}; blacklisted: {}", accessTokenId, entry.isBlacklisted());
      return entry.isBlacklisted();
    }
    LOG.debug("no entry found with id {}; assuming _not_ blacklisted", accessTokenId);
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
      StoreEntry storeEntry = byCasTicket.get(casTicket);
      LOG.debug("remove expired ticket {} with access token id {}", casTicket, storeEntry.getAccessTokenId());
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
    return new StoreEntry(id, accessToken.getSubject(), expires);
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
    private String subject;
    @XmlJavaTypeAdapter(XmlInstantAdapter.class)
    private Instant expires;
    private boolean blacklisted = false;

    public StoreEntry() {}

    public StoreEntry(String accessTokenId, String subject, Instant expires) {
      this.accessTokenId = accessTokenId;
      this.subject = subject;
      this.expires = expires;
    }

    public String getSubject() {
      return subject;
    }

    public String getAccessTokenId() {
      return accessTokenId;
    }

    public Instant getExpires() {
      return expires;
    }

    public boolean isBlacklisted() {
      return blacklisted;
    }

    public void setBlacklisted(boolean blacklisted) {
      this.blacklisted = blacklisted;
    }
  }

}
