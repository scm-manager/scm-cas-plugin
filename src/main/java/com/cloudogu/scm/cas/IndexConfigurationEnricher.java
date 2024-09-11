/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

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

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static com.cloudogu.scm.cas.CasLoginLinkProvider.createLoginLink;
import static java.util.Collections.singletonMap;
import static sonia.scm.web.VndMediaType.INDEX;

@Extension
public class IndexConfigurationEnricher extends JsonEnricherBase {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;
  private final CasContext casContext;
  private final ServiceUrlProvider serviceUrlProvider;

  @Inject
  public IndexConfigurationEnricher(Provider<ScmPathInfoStore> scmPathInfoStore, ObjectMapper objectMapper, CasContext casContext, ServiceUrlProvider serviceUrlProvider) {
    super(objectMapper);
    this.scmPathInfoStore = scmPathInfoStore;
    this.casContext = casContext;
    this.serviceUrlProvider = serviceUrlProvider;
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

      if (isCasAuthenticationEnabled() && !isCasUserAuthenticated()) {
        String loginLink = createLoginLink(casContext, serviceUrlProvider.createRoot());
        JsonNode loginNode = createObject(singletonMap("href", value(loginLink)));

        addPropertyNode(links, "casLogin", loginNode);
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
      return user.isExternal();
    }
    return false;
  }

  private String createLogoutLink() {
    return HttpUtil.append(casContext.get().getCasUrl(), "logout");
  }

}
