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
package com.cloudogu.scm.cas;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.store.InMemoryDataStore;
import sonia.scm.store.InMemoryDataStoreFactory;

import javax.xml.bind.JAXB;
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
