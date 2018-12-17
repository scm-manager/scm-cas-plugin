package com.cloudogu.scm.cas.browser;

import javax.inject.Inject;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.StringReader;

public class LogoutHandler {

  private final TicketStore ticketStore;

  @Inject
  public LogoutHandler(TicketStore ticketStore) {
    this.ticketStore = ticketStore;
  }

  public void logout(String logoutRequest) {
    LogoutRequest request = JAXB.unmarshal(new StringReader(logoutRequest), LogoutRequest.class);
    ticketStore.logout(request.sessionId);
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "LogoutRequest", namespace = "urn:oasis:names:tc:SAML:2.0:protocol")
  private static class LogoutRequest {

    @XmlElement(name = "SessionIndex", namespace = "urn:oasis:names:tc:SAML:2.0:protocol")
    private String sessionId;

  }

}
