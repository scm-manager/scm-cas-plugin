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

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.shiro.authc.AuthenticationException;
import org.jasig.cas.client.validation.Cas30ProxyTicketValidator;
import org.jasig.cas.client.validation.ProxyList;
import org.jasig.cas.client.validation.TicketValidator;

import jakarta.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class TicketValidatorFactory {

  private final CasContext context;

  @Inject
  public TicketValidatorFactory(CasContext context) {
    this.context = context;
  }

  public TicketValidator create() {
    Configuration configuration = context.get();
    if (!configuration.isEnabled()) {
      throw new AuthenticationException("cas authentication is disabled");
    }

    Cas30ProxyTicketValidator validator = new Cas30ProxyTicketValidator(configuration.getCasUrl());
    validator.setAcceptAnyProxy(configuration.isAcceptAnyProxy());

    String allowedProxyChains = configuration.getAllowedProxyChains();
    if (!Strings.isNullOrEmpty(allowedProxyChains)) {
      validator.setAllowedProxyChains(createProxyList(allowedProxyChains));
    }

    return validator;
  }

  private ProxyList createProxyList(String allowedProxyChains) {
    List<String[]> proxyList = Splitter.on('\n').trimResults()
      .omitEmptyStrings()
      .splitToList(allowedProxyChains)
      .stream()
      .map(s -> s.split("\\s"))
      .collect(Collectors.toList());
    return new ProxyList(proxyList);
  }
}
