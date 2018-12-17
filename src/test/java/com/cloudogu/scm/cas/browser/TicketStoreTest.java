package com.cloudogu.scm.cas.browser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.AccessToken;
import sonia.scm.store.InMemoryDataStore;
import sonia.scm.store.InMemoryDataStoreFactory;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketStoreTest {

  @Mock
  private AccessToken accessToken;

  @Test
  void shouldStore() {
    when(accessToken.getParentKey()).thenReturn(Optional.of("AC-123"));

    TicketStore ticketStore = new TicketStore(new InMemoryDataStoreFactory());
    ticketStore.login(CasToken.valueOf("CAS-123", "__enc__"), accessToken);

    assertFalse(ticketStore.isBlacklistet("AC-123"));

    ticketStore.logout("CAS-123");

    assertTrue(ticketStore.isBlacklistet("AC-123"));
  }

  @Test
  void loadBlacklistFromStore() {
    InMemoryDataStore<TicketStore.StoreEntry> store = new InMemoryDataStore<>();
    TicketStore.StoreEntry entry = new TicketStore.StoreEntry("AC-123");
    entry.setBlacklistet(true);
    store.put("CAS-123", entry);

    TicketStore ticketStore = new TicketStore(new InMemoryDataStoreFactory(store));
    assertTrue(ticketStore.isBlacklistet("AC-123"));
  }

}
