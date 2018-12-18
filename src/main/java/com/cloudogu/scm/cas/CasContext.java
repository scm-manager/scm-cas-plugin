package com.cloudogu.scm.cas;

import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CasContext {

  private static final String NAME = "cas";

  private ConfigurationStore<Configuration> store;

  @Inject
  public CasContext(ConfigurationStoreFactory storeFactory) {
    this.store = storeFactory.withType(Configuration.class).withName(NAME).build();
  }

  public void set(Configuration configuration) {
    ConfigurationPermissions.write(NAME).check();
    store.set(configuration);
  }

  public Configuration get() {
    Configuration configuration = store.get();
    if (configuration != null) {
      return configuration;
    }
    return new Configuration();
  }
}
