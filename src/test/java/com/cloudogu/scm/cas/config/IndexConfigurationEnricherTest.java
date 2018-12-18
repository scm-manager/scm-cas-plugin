package com.cloudogu.scm.cas.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.util.Providers;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.web.JsonEnricherContext;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.core.MediaType;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexConfigurationEnricherTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  private Subject subject;

  private IndexConfigurationEnricher enricher;

  private ObjectNode root;

  @BeforeEach
  void setUpObjectUnderTest() {
    ThreadContext.bind(subject);

    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(() -> URI.create("/"));

    enricher = new IndexConfigurationEnricher(Providers.of(pathInfoStore), objectMapper);
    root = objectMapper.createObjectNode();
    root.set("_links", objectMapper.createObjectNode());
  }

  @AfterEach
  void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldAddConfigurationLink() {
    when(subject.isPermitted("configuration:read:cas")).thenReturn(true);

    enricher.enrich(context(VndMediaType.INDEX));

    JsonNode links = root.get("_links");
    String link = links.get("casConfig").get("href").asText();
    assertThat(link).isEqualTo(ConfigurationResource.PATH);
  }

  @Test
  void shouldNotAddLinkOnWrongContentType() {
    enricher.enrich(context(VndMediaType.REPOSITORY));
    assertNoLinkWasAdded();
  }

  @Test
  void shouldNotAddLinkWithoutPermissions() {
    enricher.enrich(context(VndMediaType.INDEX));
    assertNoLinkWasAdded();
  }

  private void assertNoLinkWasAdded() {
    JsonNode links = root.get("_links");
    assertThat(links.has("casConfig")).isFalse();
  }

  private JsonEnricherContext context(String mediaType) {
    return new JsonEnricherContext(URI.create("/not/index"), MediaType.valueOf(mediaType), root);
  }

}
