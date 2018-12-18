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