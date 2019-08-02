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
