package com.cloudogu.scm.cas.config;

import com.cloudogu.scm.cas.Configuration;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurationMapperTest {

  private final URI baseUri = URI.create("https://scm.hitchhiker.com/");
  private final String expectedBaseUri = baseUri.resolve(ConfigurationResource.PATH).toASCIIString();

  private ConfigurationMapper mapper;

  @Mock
  private Subject subject;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ScmPathInfoStore scmPathInfoStore;

  @BeforeEach
  void createMapper() {
    ThreadContext.bind(subject);

    Injector injector = Guice.createInjector(
      (binder) -> binder.bind(ScmPathInfoStore.class).toInstance(scmPathInfoStore),
      new ConfigurationModule()
    );

    mapper = injector.getInstance(ConfigurationMapper.class);
    when(scmPathInfoStore.get().getApiRestUri()).thenReturn(baseUri);
  }

  @AfterEach
  void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldMapConfigurationToDto() {
    Configuration configuration = new Configuration();
    configuration.setCasUrl("http://cas.hitchhiker.com");
    configuration.setDisplayNameAttribute("cn");
    configuration.setMailAttribute("email");
    configuration.setGroupAttribute("roles");
    configuration.setEnabled(true);

    ConfigurationDto dto = mapper.toDto(configuration);
    assertThat(dto.getCasUrl()).isEqualTo(configuration.getCasUrl());
    assertThat(dto.getDisplayNameAttribute()).isEqualTo(configuration.getDisplayNameAttribute());
    assertThat(dto.getMailAttribute()).isEqualTo(configuration.getMailAttribute());
    assertThat(dto.getGroupAttribute()).isEqualTo(configuration.getGroupAttribute());
    assertThat(dto.isEnabled()).isTrue();
  }

  @Test
  void shouldMapDtoToConfiguration() {
    ConfigurationDto dto = new ConfigurationDto();
    dto.setCasUrl("http://cas.hitchhiker.com");
    dto.setDisplayNameAttribute("cn");
    dto.setMailAttribute("email");
    dto.setGroupAttribute("roles");
    dto.setEnabled(true);

    Configuration configuration = mapper.fromDto(dto);

    assertThat(configuration.getCasUrl()).isEqualTo(dto.getCasUrl());
    assertThat(configuration.getDisplayNameAttribute()).isEqualTo(dto.getDisplayNameAttribute());
    assertThat(configuration.getMailAttribute()).isEqualTo(dto.getMailAttribute());
    assertThat(configuration.getGroupAttribute()).isEqualTo(dto.getGroupAttribute());
    assertThat(configuration.isEnabled()).isTrue();
  }

  @Test
  void shouldAddSelfLink() {
    ConfigurationDto dto = mapper.toDto(new Configuration());
    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo(expectedBaseUri);
  }

  @Test
  void shouldNotAddUpdateLink() {
    ConfigurationDto dto = mapper.toDto(new Configuration());
    assertThat(dto.getLinks().getLinkBy("update")).isNotPresent();
  }

  @Test
  void shouldAddUpdateLinkIfPermitted() {
    when(subject.isPermitted("configuration:write:cas")).thenReturn(true);
    ConfigurationDto dto = mapper.toDto(new Configuration());
    assertThat(dto.getLinks().getLinkBy("update").get().getHref()).isEqualTo(expectedBaseUri);
  }

}
