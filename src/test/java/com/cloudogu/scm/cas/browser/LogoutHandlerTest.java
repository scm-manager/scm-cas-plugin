package com.cloudogu.scm.cas.browser;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LogoutHandlerTest {

  @Mock
  private TicketStore ticketStore;

  @InjectMocks
  private LogoutHandler handler;

  @Test
  void shouldLogout() throws IOException {
    URL resource = Resources.getResource("com/cloudogu/scm/cas/browser/logoutRequest.xml");
    String logoutRequest = Resources.toString(resource, Charsets.UTF_8);

    handler.logout(logoutRequest);

    verify(ticketStore).logout("ST-8-L66D4LTpMGDptQ7kLark-f77b8125e3a6");
  }

}
