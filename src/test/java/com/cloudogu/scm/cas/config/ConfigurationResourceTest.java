/*
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
package com.cloudogu.scm.cas.config;

import com.cloudogu.scm.cas.CasContext;
import com.cloudogu.scm.cas.Configuration;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationResourceTest {

  @Mock
  private Subject subject;

  @Mock
  private CasContext context;

  @Mock
  private ConfigurationMapper mapper;

  @InjectMocks
  private ConfigurationResource resource;

  @BeforeEach
  void setUpObjectUnderTest() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldThrowAuthorizationExceptionWithoutReadPermission() {
    doThrow(AuthorizationException.class).when(subject).checkPermission("configuration:read:cas");
    assertThrows(AuthorizationException.class, () -> resource.get());
  }

  @Test
  void shouldThrowAuthorizationExceptionWithoutWritePermissions() {
    doThrow(AuthorizationException.class).when(subject).checkPermission("configuration:write:cas");
    assertThrows(AuthorizationException.class, () -> resource.update(new ConfigurationDto()));
  }

  @Test
  void shouldReturnMappedConfiguration() {
    Configuration configuration = new Configuration();
    when(context.get()).thenReturn(configuration);

    ConfigurationDto configurationDto = new ConfigurationDto();
    when(mapper.toDto(configuration)).thenReturn(configurationDto);

    ConfigurationDto response = resource.get();
    assertThat(response).isSameAs(configurationDto);
  }

  @Test
  void shouldStoreUnmappedConfiguration() {
    ConfigurationDto dto = new ConfigurationDto();
    Configuration configuration = new Configuration();
    when(mapper.fromDto(dto)).thenReturn(configuration);

    Response response = resource.update(dto);
    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);

    verify(context).set(configuration);
  }
}
