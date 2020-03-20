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
import com.cloudogu.scm.cas.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.InMemoryConfigurationStore;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import java.net.URI;
import java.util.Optional;

import static java.net.URI.create;
import static org.assertj.core.api.Assertions.assertThat;

class AfterLogoutRedirectToCasTest {

  private AfterLogoutRedirectToCas afterLogoutRedirectToCas;
  private Configuration configuration;

  @BeforeEach
  void initConfig() {
    InMemoryConfigurationStoreFactory storeFactory = new InMemoryConfigurationStoreFactory();
    ConfigurationStore store = storeFactory.withType(Configuration.class).withName(Constants.NAME).build();
    configuration = new Configuration();
    store.set(configuration);
    afterLogoutRedirectToCas = new AfterLogoutRedirectToCas(new CasContext(storeFactory));
  }

  @Test
  void shouldCreateNothingWhenDisabled() {
    configuration.setEnabled(false);
    configuration.setCasUrl("http://example.com/cas");

    Optional<URI> uri = afterLogoutRedirectToCas.afterLogoutRedirectTo();

    assertThat(uri).isEmpty();
  }

  @Test
  void shouldCreateLogoutUri() {
    configuration.setEnabled(true);
    configuration.setCasUrl("http://example.com/cas");

    Optional<URI> uri = afterLogoutRedirectToCas.afterLogoutRedirectTo();

    assertThat(uri).get().isEqualTo(create("http://example.com/cas/logout"));
  }

  @Test
  void shouldCreateLogoutUriWithTrailingSlash() {
    configuration.setEnabled(true);
    configuration.setCasUrl("http://example.com/cas/");

    Optional<URI> uri = afterLogoutRedirectToCas.afterLogoutRedirectTo();

    assertThat(uri).get().isEqualTo(create("http://example.com/cas/logout"));
  }
}
