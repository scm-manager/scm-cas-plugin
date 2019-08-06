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
      assertThat(user.getType()).isEqualTo("cas");
    }

    @Test
    void shouldCreateUserWithoutDisplayName() {
      configuration.setDisplayNameAttribute("cn");

      User user = mapper.createUser(assertion);

      assertThat(user.getName()).isEqualTo("tricia");
      assertThat(user.getDisplayName()).isEqualTo(principal.getName());
      assertThat(user.getMail()).isEqualTo("tricia.mcmillan@hitchhiker.com");
      assertThat(user.getType()).isEqualTo("cas");
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
