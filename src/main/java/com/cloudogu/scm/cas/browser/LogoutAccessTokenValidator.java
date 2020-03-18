/**
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenValidator;

import javax.inject.Inject;

@Extension
public class LogoutAccessTokenValidator implements AccessTokenValidator {

  private static final Logger LOG = LoggerFactory.getLogger(LogoutAccessTokenValidator.class);

  private final TicketStore ticketStore;

  @Inject
  public LogoutAccessTokenValidator(TicketStore ticketStore) {
    this.ticketStore = ticketStore;
  }

  @Override
  public boolean validate(AccessToken token) {
    LOG.trace("checking whether token {} with parent id {} is valid", token.getId(), token.getParentKey());
    String id = resolveId(token);
    return !ticketStore.isBlacklisted(id);
  }

  private String resolveId(AccessToken token) {
    return token.getParentKey().orElse(token.getId());
  }
}
