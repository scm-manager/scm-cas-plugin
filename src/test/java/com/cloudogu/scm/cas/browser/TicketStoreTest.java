package com.cloudogu.scm.cas.browser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.AccessToken;
import sonia.scm.store.InMemoryDataStore;
import sonia.scm.store.InMemoryDataStoreFactory;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketStoreTest {

  @Mock
  private AccessToken accessToken;

  @Test
  void shouldStore() {
    Instant instant = Instant.now();
    when(accessToken.getExpiration()).thenReturn(Date.from(instant));
    when(accessToken.getParentKey()).thenReturn(Optional.of("AC-123"));

    TicketStore ticketStore = new TicketStore(new InMemoryDataStoreFactory());
    ticketStore.login(CasToken.valueOf("CAS-123", "__enc__"), accessToken);

    assertFalse(ticketStore.isBlacklisted("AC-123"));

    ticketStore.logout("CAS-123");

    assertTrue(ticketStore.isBlacklisted("AC-123"));
  }

  @Test
  void shouldLoadBlacklistFromStore() {
    InMemoryDataStore<TicketStore.StoreEntry> store = new InMemoryDataStore<>();
    TicketStore.StoreEntry entry = new TicketStore.StoreEntry("AC-123", Instant.now());
    entry.setBlacklistet(true);
    store.put("CAS-123", entry);

    TicketStore ticketStore = new TicketStore(new InMemoryDataStoreFactory(store));
    assertTrue(ticketStore.isBlacklisted("AC-123"));
  }

  @Test
  void shouldRemoveTheExpiredTicket() {
    when(accessToken.getId()).thenReturn("AC-456");

    LocalDateTime issuedAt = LocalDateTime.of(2018, 12, 18, 8, 7, 42, 0);
    when(accessToken.getIssuedAt()).thenReturn(toDate(issuedAt));

    LocalDateTime expiration = issuedAt.plusHours(1);
    when(accessToken.getExpiration()).thenReturn(toDate(expiration));

    LocalDateTime refreshExpiration = issuedAt.plusHours(10);
    when(accessToken.getRefreshExpiration()).thenReturn(toOptionalDate(refreshExpiration));


    InMemoryDataStore<TicketStore.StoreEntry> store = new InMemoryDataStore<>();
    LocalDateTime actual = issuedAt.plusHours(11).plusSeconds(1);

    // add not expired ticket
    TicketStore.StoreEntry entry = new TicketStore.StoreEntry("AC-123", toInstant(actual.plusHours(1)));
    entry.setBlacklistet(true);
    store.put("CAS-123", entry);

    Clock clock = mock(Clock.class);
    when(clock.instant()).thenReturn(toInstant(actual));

    TicketStore ticketStore = new TicketStore(new InMemoryDataStoreFactory(store), clock);
    ticketStore.login(CasToken.valueOf("CAS-456", "__env__"), accessToken);
    ticketStore.removeExpired();

    assertNotNull(store.get("CAS-123"));
    assertNull(store.get("CAS-456"));
  }

  @Test
  void shouldNotRemoveTheTicket() {
    when(accessToken.getId()).thenReturn("AC-123");

    LocalDateTime issuedAt = LocalDateTime.of(2018, 12, 18, 8, 7, 42, 0);
    when(accessToken.getIssuedAt()).thenReturn(toDate(issuedAt));

    LocalDateTime expiration = issuedAt.plusHours(1);
    when(accessToken.getExpiration()).thenReturn(toDate(expiration));

    LocalDateTime refreshExpiration = issuedAt.plusHours(10);
    when(accessToken.getRefreshExpiration()).thenReturn(toOptionalDate(refreshExpiration));

    InMemoryDataStore<TicketStore.StoreEntry> store = new InMemoryDataStore<>();
    LocalDateTime actual = issuedAt.plusHours(11).minusSeconds(2);

    Clock clock = mock(Clock.class);
    when(clock.instant()).thenReturn(toInstant(actual));

    TicketStore ticketStore = new TicketStore(new InMemoryDataStoreFactory(store), clock);
    ticketStore.login(CasToken.valueOf("CAS-123", "__env__"), accessToken);
    ticketStore.removeExpired();

    assertNotNull(store.get("CAS-123"));
  }

  private Instant toInstant(LocalDateTime localDateTime) {
    return localDateTime.toInstant(ZoneOffset.UTC);
  }

  private Date toDate(LocalDateTime localDateTime) {
    return Date.from(toInstant(localDateTime));
  }

  private Optional<Date> toOptionalDate(LocalDateTime localDateTime) {
    if (localDateTime != null) {
      return Optional.of(toDate(localDateTime));
    }
    return Optional.empty();
  }

}
