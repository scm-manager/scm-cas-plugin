package com.cloudogu.scm.cas;


import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.InMemoryConfigurationStore;
import sonia.scm.store.InMemoryConfigurationStoreFactory;
import org.apache.shiro.util.ThreadContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CasContextTest {

  private ConfigurationStore<Configuration> store;

  private CasContext context;

  @BeforeEach
  void setUpObjectUnderTest() {
    store = new InMemoryConfigurationStore<>();
    InMemoryConfigurationStoreFactory inMemoryConfigurationStoreFactory = new InMemoryConfigurationStoreFactory(store);
    context = new CasContext(inMemoryConfigurationStoreFactory);
  }

  @Test
  void shouldReturnInitialConfiguration() {
    Configuration configuration = context.get();
    assertThat(configuration).isNotNull();
  }

  @Test
  void shouldReturnStoredConfiguration() {
    Configuration configuration = new Configuration();
    store.set(configuration);

    Configuration result = context.get();
    assertThat(result).isSameAs(configuration);
  }

  @Nested
  class SetTests {

    @Mock
    private Subject subject;

    @BeforeEach
    void bindSubject() {
      ThreadContext.bind(subject);
    }

    @AfterEach
    void unbindSubject() {
      ThreadContext.unbindSubject();
    }

    @Test
    void shouldFailIfTheUserIsNotPermitted() {
      doThrow(AuthorizationException.class).when(subject).checkPermission("configuration:write:cas");

      assertThrows(AuthorizationException.class, () -> context.set(new Configuration()));
    }

    @Test
    void shouldSetTheConfiguration() {
      Configuration configuration = new Configuration();
      context.set(configuration);

      Configuration result = context.get();
      assertThat(result).isSameAs(configuration);
    }

  }

}
