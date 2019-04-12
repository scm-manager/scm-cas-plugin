package com.cloudogu.scm.cas.rest;

import com.cloudogu.scm.cas.CasContext;
import com.cloudogu.scm.cas.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    InMemoryConfigurationStore store = new InMemoryConfigurationStore();
    configuration = new Configuration();
    store.set(configuration);
    afterLogoutRedirectToCas = new AfterLogoutRedirectToCas(new CasContext(new InMemoryConfigurationStoreFactory(store)));
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
