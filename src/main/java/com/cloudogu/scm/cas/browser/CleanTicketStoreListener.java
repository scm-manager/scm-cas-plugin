package com.cloudogu.scm.cas.browser;

import com.google.common.annotations.VisibleForTesting;
import sonia.scm.plugin.Extension;
import sonia.scm.schedule.Scheduler;
import sonia.scm.schedule.Task;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

@Extension
public class CleanTicketStoreListener implements ServletContextListener {

  @VisibleForTesting
  static final String CRON = "0 0/5 * * * ?";

  private final Scheduler scheduler;

  private Task task;

  @Inject
  public CleanTicketStoreListener(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    task = scheduler.schedule(CRON, CleanTicketStoreTask.class);
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    task.cancel();
  }
}
