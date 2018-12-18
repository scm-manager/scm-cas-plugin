package com.cloudogu.scm.cas.browser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CleanTicketStoreTaskTest {

  @Mock
  private TicketStore ticketStore;

  @InjectMocks
  private CleanTicketStoreTask cleanTicketStoreTask;

  @Test
  void shouldCallTicketStoreRemoveExpired() {
    cleanTicketStoreTask.run();

    verify(ticketStore).removeExpired();
  }

}
