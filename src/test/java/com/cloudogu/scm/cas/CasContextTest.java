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


import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.InMemoryConfigurationStoreFactory;
import org.apache.shiro.util.ThreadContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class CasContextTest {

  private ConfigurationStore<Configuration> store;



  private CasContext context;

  @BeforeEach
  void setUpObjectUnderTest() {
    InMemoryConfigurationStoreFactory inMemoryConfigurationStoreFactory = new InMemoryConfigurationStoreFactory();
    store = inMemoryConfigurationStoreFactory.withType(Configuration.class).withName(Constants.NAME).build();
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
