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
