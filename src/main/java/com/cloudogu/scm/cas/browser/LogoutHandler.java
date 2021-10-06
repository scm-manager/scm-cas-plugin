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

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.event.ScmEventBus;
import sonia.scm.security.LogoutEvent;

import javax.inject.Inject;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.StringReader;

public class LogoutHandler {

  private static final Logger LOG = LoggerFactory.getLogger(LogoutHandler.class);

  private final TicketStore ticketStore;
  private final ScmEventBus eventBus;

  @Inject
  public LogoutHandler(TicketStore ticketStore, ScmEventBus eventBus) {
    this.ticketStore = ticketStore;
    this.eventBus = eventBus;
  }

  public void logout(String logoutRequest) {
    LogoutRequest request = JAXB.unmarshal(new StringReader(logoutRequest), LogoutRequest.class);
    ticketStore.logout(request.sessionId);

    String username = request.username;
    if (isValidUsername(username)) {
      LOG.debug("Logout cas user with username: {}", username);
      eventBus.post(new LogoutEvent(username));
    } else {
      LOG.debug("slo request does not contain a valid username, skip sending logout event");
    }
  }

  private boolean isValidUsername(String username) {
    // The spec is not really clear about an implementation has to send the username.
    // So we are test if it is really set and not the strange @NOT_USED@ of the examples.
    // https://apereo.github.io/cas/6.4.x/installation/Logout-Single-Signout.html#logout-and-single-logout-slo
    return !Strings.isNullOrEmpty(username) && !username.equals("@NOT_USED@");
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "LogoutRequest", namespace = "urn:oasis:names:tc:SAML:2.0:protocol")
  private static class LogoutRequest {

    @XmlElement(name = "SessionIndex", namespace = "urn:oasis:names:tc:SAML:2.0:protocol")
    private String sessionId;

    @XmlElement(name = "NameID", namespace = "urn:oasis:names:tc:SAML:2.0:assertion")
    private String username;

  }

}
