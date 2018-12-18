package com.cloudogu.scm.cas.browser;

import javax.inject.Inject;

public class CleanTicketStoreTask implements Runnable {

  private TicketStore ticketStore;

  @Inject
  public CleanTicketStoreTask(TicketStore ticketStore) {
    this.ticketStore = ticketStore;
  }

  @Override
  public void run() {
    ticketStore.removeExpired();
  }
}
