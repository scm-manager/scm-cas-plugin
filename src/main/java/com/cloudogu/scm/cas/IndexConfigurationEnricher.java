package com.cloudogu.scm.cas;

import com.cloudogu.scm.cas.config.ConfigurationResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.plugin.Extension;
import sonia.scm.user.User;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.JsonEnricherBase;
import sonia.scm.web.JsonEnricherContext;

import javax.inject.Inject;
import javax.inject.Provider;

import static java.util.Collections.singletonMap;
import static sonia.scm.web.VndMediaType.INDEX;

@Extension
public class IndexConfigurationEnricher extends JsonEnricherBase {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;
  private final CasContext casContext;

  @Inject
  public IndexConfigurationEnricher(Provider<ScmPathInfoStore> scmPathInfoStore, ObjectMapper objectMapper, CasContext casContext) {
    super(objectMapper);
    this.scmPathInfoStore = scmPathInfoStore;
    this.casContext = casContext;
  }

  @Override
  public void enrich(JsonEnricherContext context) {
    if (resultHasMediaType(INDEX, context)) {
      JsonNode links = context.getResponseEntity().get("_links");

      if (ConfigurationPermissions.read().isPermitted(Constants.NAME)) {
        String configUrl = new LinkBuilder(scmPathInfoStore.get().get(), ConfigurationResource.class)
          .method("get")
          .parameters()
          .href();

        JsonNode gitConfigRefNode = createObject(singletonMap("href", value(configUrl)));

        addPropertyNode(links, "casConfig", gitConfigRefNode);
      }

      if (isCasAuthenticationEnabled() && isCasUserAuthenticated()) {
        JsonNode logoutNode = createObject(singletonMap("href", value(createLogoutLink())));

        addPropertyNode(links, "casLogout", logoutNode);
      }

    }
  }

  private boolean isCasAuthenticationEnabled() {
    return casContext.get().isEnabled();
  }

  private boolean isCasUserAuthenticated() {
    Subject subject = SecurityUtils.getSubject();
    if (subject.isAuthenticated()) {
      User user = subject.getPrincipals().oneByType(User.class);
      return Constants.NAME.equals(user.getType());
    }
    return false;
  }

  private String createLogoutLink() {
    return HttpUtil.append(casContext.get().getCasUrl(), "logout");
  }
}
