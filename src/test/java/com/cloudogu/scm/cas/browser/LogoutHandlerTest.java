/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.cloudogu.scm.cas.browser;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.event.ScmEventBus;
import sonia.scm.security.LogoutEvent;

import java.io.IOException;
import java.net.URL;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class LogoutHandlerTest {

  @Mock
  private TicketStore ticketStore;

  @Mock
  private ScmEventBus eventBus;

  @InjectMocks
  private LogoutHandler handler;

  @Test
  void shouldLogout() throws IOException {
    logout("slo-trillian");

    verify(ticketStore).logout("ST-8-L66D4LTpMGDptQ7kLark-f77b8125e3a6");
    verify(eventBus).post(new LogoutEvent("trillian"));
  }

  private void logout(String requestName) throws IOException {
    String logoutRequest = read(requestName);

    handler.logout(logoutRequest);
  }

  @Test
  void shouldNotFireLogoutEventWithoutNameID() throws IOException {
    logout("slo-without-nameid");

    verifyNoInteractions(eventBus);
  }

  @Test
  void shouldNotFireLogoutEventWithoutNotUsedNameID() throws IOException {
    logout("slo-nameid-not-used");

    verifyNoInteractions(eventBus);
  }

  @SuppressWarnings("UnstableApiUsage")
  private String read(String name) throws IOException {
    URL resource = Resources.getResource("com/cloudogu/scm/cas/browser/" + name + ".xml");
    return Resources.toString(resource, Charsets.UTF_8);
  }

}
