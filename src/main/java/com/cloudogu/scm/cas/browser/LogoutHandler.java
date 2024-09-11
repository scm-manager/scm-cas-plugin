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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.event.ScmEventBus;
import sonia.scm.security.LogoutEvent;

import jakarta.inject.Inject;
import jakarta.xml.bind.JAXB;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.StringReader;
import java.util.Optional;

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
    Optional<String> subject = ticketStore.logout(request.sessionId);
    if (subject.isPresent()) {
      LOG.debug("logout cas user {}", subject.get());
      eventBus.post(new LogoutEvent(subject.get()));
    } else {
      LOG.debug("received cas logout, but no subject is stored with the ticket. Skip fire logout event.");
    }
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "LogoutRequest", namespace = "urn:oasis:names:tc:SAML:2.0:protocol")
  private static class LogoutRequest {

    @XmlElement(name = "SessionIndex", namespace = "urn:oasis:names:tc:SAML:2.0:protocol")
    private String sessionId;

  }

}
