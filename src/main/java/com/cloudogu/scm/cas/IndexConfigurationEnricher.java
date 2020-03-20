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
