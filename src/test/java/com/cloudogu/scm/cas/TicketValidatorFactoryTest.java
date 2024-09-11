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

package com.cloudogu.scm.cas;

import org.apache.shiro.authc.AuthenticationException;
import org.jasig.cas.client.validation.Cas30ProxyTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketValidatorFactoryTest {

  @Mock
  private CasContext casContext;

  @Test
  void shouldReturnCas30TicketValidator() {
    bindConfiguration(true);

    TicketValidatorFactory factory = new TicketValidatorFactory(casContext);
    TicketValidator ticketValidator = factory.create();

    assertThat(ticketValidator).isInstanceOf(Cas30ProxyTicketValidator.class);
  }

  @Test
  void shouldThrowAuthenticationExceptionIfCasAuthenticationIsDisabled() {
    bindConfiguration(false);
    TicketValidatorFactory factory = new TicketValidatorFactory(casContext);

    assertThrows(AuthenticationException.class, () -> factory.create());
  }

  private void bindConfiguration(boolean b) {
    Configuration configuration = new Configuration();
    configuration.setCasUrl("https://hitchhiker.com/cas");
    configuration.setEnabled(b);

    when(casContext.get()).thenReturn(configuration);
  }


}
