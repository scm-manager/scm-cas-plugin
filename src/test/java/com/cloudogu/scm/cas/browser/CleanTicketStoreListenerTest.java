package com.cloudogu.scm.cas.browser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.schedule.Scheduler;
import sonia.scm.schedule.Task;

import javax.servlet.ServletContextEvent;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CleanTicketStoreListenerTest {

  @Mock
  private Scheduler scheduler;

  @Mock
  private Task task;

  @Mock
  private ServletContextEvent event;

  @InjectMocks
  private CleanTicketStoreListener listener;

  @Test
  void shouldScheduleAndCancel() {
    when(scheduler.schedule(CleanTicketStoreListener.CRON, CleanTicketStoreTask.class)).thenReturn(task);

    listener.contextInitialized(event);
    listener.contextDestroyed(event);

    verify(task).cancel();
  }

}
