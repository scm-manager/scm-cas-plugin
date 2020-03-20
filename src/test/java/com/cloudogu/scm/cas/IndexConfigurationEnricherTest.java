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
package com.cloudogu.scm.cas;

import com.cloudogu.scm.cas.config.ConfigurationResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.util.Providers;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;
import sonia.scm.web.JsonEnricherContext;
import sonia.scm.web.VndMediaType;

import javax.ws.rs.core.MediaType;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IndexConfigurationEnricherTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  private Subject subject;

  @Mock
  private CasContext context;

  private Configuration configuration;

  private IndexConfigurationEnricher enricher;

  private ObjectNode root;

  @BeforeEach
  void setUpObjectUnderTest() {
    ThreadContext.bind(subject);

    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(() -> URI.create("/"));

    configuration = new Configuration();
    when(context.get()).thenReturn(configuration);

    enricher = new IndexConfigurationEnricher(Providers.of(pathInfoStore), objectMapper, context);
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
    assertThat(link).isEqualTo("/" + ConfigurationResource.PATH);
  }


  @Test
  void shouldAddCasLogoutLink() {
    configuration.setEnabled(true);
    configuration.setCasUrl("https://cas.hitchhiker.com");

    when(subject.isAuthenticated()).thenReturn(true);
    mockTrillianAuthentication("cas");

    enricher.enrich(context(VndMediaType.INDEX));

    JsonNode links = root.get("_links");
    String link = links.get("casLogout").get("href").asText();
    assertThat(link).isEqualTo("https://cas.hitchhiker.com/logout");
  }

  @Test
  void shouldNotAddCasLogoutIfCasIsDisabled() {
    configuration.setEnabled(false);
    configuration.setCasUrl("https://cas.hitchhiker.com");

    enricher.enrich(context(VndMediaType.INDEX));

    assertNoLogoutLinkWasAdded();
  }

  @Test
  void shouldNotAddCasLogoutForNonCasUsers() {
    configuration.setEnabled(true);
    configuration.setCasUrl("https://cas.hitchhiker.com");

    when(subject.isAuthenticated()).thenReturn(true);
    mockTrillianAuthentication("xml");

    enricher.enrich(context(VndMediaType.INDEX));

    assertNoLogoutLinkWasAdded();
  }

  @Test
  void shouldNotAddCasLogoutLinkIfUnauthenticated() {
    when(subject.isAuthenticated()).thenReturn(false);

    enricher.enrich(context(VndMediaType.INDEX));

    assertNoLogoutLinkWasAdded();
  }

  private void assertNoLogoutLinkWasAdded() {
    JsonNode links = root.get("_links");
    assertThat(links.has("casLogout")).isFalse();
  }

  private void mockTrillianAuthentication(String type) {
    User trillian = UserTestData.createTrillian();
    trillian.setType(type);
    mockAuthentication(trillian);
  }

  private void mockAuthentication(User user) {
    PrincipalCollection principalCollection = mock(PrincipalCollection.class);
    when(principalCollection.oneByType(User.class)).thenReturn(user);
    when(subject.getPrincipals()).thenReturn(principalCollection);
  }

  @Test
  void shouldNotAddLinkOnWrongContentType() {
    enricher.enrich(context(VndMediaType.REPOSITORY));
    assertNoConfigLinkWasAdded();
  }

  @Test
  void shouldNotAddLinkWithoutPermissions() {
    enricher.enrich(context(VndMediaType.INDEX));
    assertNoConfigLinkWasAdded();
  }

  private void assertNoConfigLinkWasAdded() {
    JsonNode links = root.get("_links");
    assertThat(links.has("casConfig")).isFalse();
  }

  private JsonEnricherContext context(String mediaType) {
    return new JsonEnricherContext(URI.create("/not/index"), MediaType.valueOf(mediaType), root);
  }

}
