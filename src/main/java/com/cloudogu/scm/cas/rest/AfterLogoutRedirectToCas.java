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
package com.cloudogu.scm.cas.rest;

import com.cloudogu.scm.cas.CasContext;
import com.cloudogu.scm.cas.Configuration;
import sonia.scm.api.v2.resources.LogoutRedirection;
import sonia.scm.plugin.Extension;

import javax.inject.Inject;
import java.net.URI;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Extension
public class AfterLogoutRedirectToCas implements LogoutRedirection {

  private final CasContext context;

  @Inject
  public AfterLogoutRedirectToCas(CasContext context) {
    this.context = context;
  }

  @Override
  public Optional<URI> afterLogoutRedirectTo() {
    Configuration configuration = context.get();
    if (configuration.isEnabled()) {
      String casBaseUrl = configuration.getCasUrl();
      if (!casBaseUrl.endsWith("/")) {
        casBaseUrl = casBaseUrl + "/";
      }
      return of(URI.create(casBaseUrl).resolve("./logout"));
    } else {
      return empty();
    }
  }
}
