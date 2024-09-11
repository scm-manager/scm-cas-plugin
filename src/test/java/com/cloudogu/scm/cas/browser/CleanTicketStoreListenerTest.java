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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.schedule.Scheduler;
import sonia.scm.schedule.Task;

import jakarta.servlet.ServletContextEvent;

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
