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

import com.google.common.collect.ImmutableMap;
import org.assertj.core.util.Lists;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.user.User;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssertionMapperTest {

  private static final LinkedList mails = new LinkedList(Arrays.asList("tricia.mcmillan@hitchhiker.com", "tricia@hitchhiker.com"));

  private static final Map<String, Object> TRICIA_ATTRIBUTES = ImmutableMap.of(
    "displayName", "Tricia McMillan",
    "mail", mails
  );

  @Mock
  private AttributePrincipal principal;

  @Mock
  private Assertion assertion;

  private Configuration configuration;

  @Mock
  private CasContext casContext;

  private AssertionMapper mapper;

  @BeforeEach
  void setUpObjectUnderTest() {
    when(assertion.getPrincipal()).thenReturn(principal);

    configuration = new Configuration();
    when(casContext.get()).thenReturn(configuration);
    mapper = new AssertionMapper(casContext);
  }

  @Nested
  class UserMapping {

    @BeforeEach
    void setUpMocks() {
      when(principal.getName()).thenReturn("tricia");
      when(principal.getAttributes()).thenReturn(TRICIA_ATTRIBUTES);
    }

    @Test
    void shouldCreateUser() {
      User user = mapper.createUser(assertion);

      assertThat(user.getName()).isEqualTo("tricia");
      assertThat(user.getDisplayName()).isEqualTo("Tricia McMillan");
      assertThat(user.getMail()).isEqualTo("tricia.mcmillan@hitchhiker.com");
      assertThat(user.isExternal()).isTrue();
    }

    @Test
    void shouldCreateUserWithoutDisplayName() {
      configuration.setDisplayNameAttribute("cn");

      User user = mapper.createUser(assertion);

      assertThat(user.getName()).isEqualTo("tricia");
      assertThat(user.getDisplayName()).isEqualTo(principal.getName());
      assertThat(user.getMail()).isEqualTo("tricia.mcmillan@hitchhiker.com");
      assertThat(user.isExternal()).isTrue();
    }

    @Test
    void shouldCreateUserWithoutEmailIfInvalid() {
      configuration.setMailAttribute("cn");

      User user = mapper.createUser(assertion);

      assertThat(user.getMail()).isNull();
    }
  }

  @Nested
  class GroupMapping {

    @Test
    void shouldCreateACollectionOfGroups() {
      Map<String, Object> attributes = ImmutableMap.of("groups", Lists.newArrayList("one", "two"));
      when(principal.getAttributes()).thenReturn(attributes);

      Collection<String> groups = mapper.createGroups(assertion);
      assertThat(groups).containsOnly("one", "two");
    }

    @Test
    void shouldCreateACollectionOfASingleGroup() {
      Map<String, Object> attributes = ImmutableMap.of("groups", "one");
      when(principal.getAttributes()).thenReturn(attributes);

      Collection<String> groups = mapper.createGroups(assertion);
      assertThat(groups).containsOnly("one");
    }

    @Test
    void shouldReturnEmptyCollectionWithoutGroupAttribute() {
      Map<String, Object> attributes = ImmutableMap.of();
      when(principal.getAttributes()).thenReturn(attributes);

      Collection<String> groups = mapper.createGroups(assertion);
      assertThat(groups).isEmpty();
    }
  }

}
