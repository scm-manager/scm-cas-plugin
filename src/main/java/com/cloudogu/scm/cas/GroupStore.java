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

import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
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
  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  static class Groups {
    @XmlElement(name = "name")
    Set<String> groups;
  }
}
