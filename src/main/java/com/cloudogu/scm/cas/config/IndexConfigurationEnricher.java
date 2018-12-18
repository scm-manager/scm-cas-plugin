package com.cloudogu.scm.cas.config;

import com.cloudogu.scm.cas.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.plugin.Extension;
import sonia.scm.web.JsonEnricherBase;
import sonia.scm.web.JsonEnricherContext;

import javax.inject.Inject;
import javax.inject.Provider;

import static java.util.Collections.singletonMap;
import static sonia.scm.web.VndMediaType.INDEX;

@Extension
public class IndexConfigurationEnricher extends JsonEnricherBase {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;

  @Inject
  public IndexConfigurationEnricher(Provider<ScmPathInfoStore> scmPathInfoStore, ObjectMapper objectMapper) {
    super(objectMapper);
    this.scmPathInfoStore = scmPathInfoStore;
  }

  @Override
  public void enrich(JsonEnricherContext context) {
    if (resultHasMediaType(INDEX, context) && ConfigurationPermissions.read().isPermitted(Constants.NAME)) {
      String configUrl = new LinkBuilder(scmPathInfoStore.get().get(), ConfigurationResource.class)
        .method("get")
        .parameters()
        .href();

      JsonNode gitConfigRefNode = createObject(singletonMap("href", value(configUrl)));

      addPropertyNode(context.getResponseEntity().get("_links"), "casConfig", gitConfigRefNode);
    }
  }
}
