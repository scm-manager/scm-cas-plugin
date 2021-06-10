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
package com.cloudogu.scm.cas;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.shiro.authc.AuthenticationException;
import org.jasig.cas.client.validation.Cas30ProxyTicketValidator;
import org.jasig.cas.client.validation.ProxyList;
import org.jasig.cas.client.validation.TicketValidator;

import javax.inject.Inject;
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
