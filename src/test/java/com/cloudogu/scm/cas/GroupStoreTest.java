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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.store.InMemoryDataStore;
import sonia.scm.store.InMemoryDataStoreFactory;

import jakarta.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GroupStoreTest {

  private GroupStore groupStore;

  @BeforeEach
  void setUpObjectUnderTesting() {
    InMemoryDataStore<GroupStore.Groups> store = new InMemoryDataStore<>();
    DataStoreFactory storeFactory = new InMemoryDataStoreFactory(store);
    groupStore = new GroupStore(storeFactory);
  }

  @Test
  void shouldPutAndGet() {
    Set<String> groups = ImmutableSet.of("heartOfGold", "puzzle42");
    groupStore.put("trillian", groups);

    Set<String> storedGroups = groupStore.get("trillian");
    assertThat(storedGroups).containsExactlyInAnyOrderElementsOf(groups);
  }

  @Test
  void shouldReturnEmptySet() {
    Set<String> storedGroups = groupStore.get("trillian");
    assertThat(storedGroups).isEmpty();
  }

  @Test
  void shouldBeAbleToMarshalAndUnmarshalGroups() {
    Set<String> groups = ImmutableSet.of("heartOfGold", "puzzle42");
    GroupStore.Groups storeGroups = new GroupStore.Groups(groups);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JAXB.marshal(storeGroups, baos);

    System.out.println(baos.toString());

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    GroupStore.Groups unmarshaledGroups = JAXB.unmarshal(bais, GroupStore.Groups.class);

    assertThat(unmarshaledGroups.groups).containsExactlyInAnyOrderElementsOf(groups);
  }

}
