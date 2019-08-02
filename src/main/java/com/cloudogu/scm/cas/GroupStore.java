package com.cloudogu.scm.cas;

import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.Set;

@Singleton
public class GroupStore {

  private final static String STORE_NAME = "casGroups";
  private final DataStore<Groups> store;

  @Inject
  public GroupStore(DataStoreFactory factory) {
    this.store = factory.withType(Groups.class).withName(STORE_NAME).build();
  }

  public Set<String> get(String principal){
    Groups groups = store.get(principal);
    return groups != null ? groups.groups : ImmutableSet.of();
  }

  public void put(String principal, Set<String> groups) {
    store.put(principal, new Groups(groups));
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @XmlAccessorType(XmlAccessType.FIELD)
  static class Groups {
    @XmlElement(name = "name")
    Set<String> groups;
  }
}
