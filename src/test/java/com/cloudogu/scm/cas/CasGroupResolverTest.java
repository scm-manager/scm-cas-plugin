package com.cloudogu.scm.cas;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CasGroupResolverTest {

  @Mock
  private GroupStore store;

  @InjectMocks
  private CasGroupResolver resolver;

  @Test
  void shouldReturnGroupsFromStore() {
    Set<String> groups = ImmutableSet.of("heartOfGold", "puzzle42");
    when(store.get("trillian")).thenReturn(groups);

    Set<String> resolvedGroups = resolver.resolve("trillian");
    assertThat(resolvedGroups).containsExactlyInAnyOrderElementsOf(groups);
  }

}
