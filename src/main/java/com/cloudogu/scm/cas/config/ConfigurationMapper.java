package com.cloudogu.scm.cas.config;

import com.cloudogu.scm.cas.Configuration;
import com.cloudogu.scm.cas.Constants;
import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ConfigurationPermissions;

import javax.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class ConfigurationMapper {

  abstract Configuration fromDto(ConfigurationDto dto);

  abstract ConfigurationDto toDto(Configuration configuration);

  @Inject
  private ScmPathInfoStore scmPathInfoStore;

  @AfterMapping
  void appendLinks(Configuration configuration, @MappingTarget ConfigurationDto dto) {
    Links.Builder linksBuilder = linkingTo().self(self());
    if (ConfigurationPermissions.write(Constants.NAME).isPermitted()) {
      linksBuilder.single(link("update", update()));
    }
    dto.add(linksBuilder.build());
  }

  private String self() {
    return linkBuilder().method("get").parameters().href();
  }

  private String update() {
    return linkBuilder().method("update").parameters().href();
  }

  private LinkBuilder linkBuilder() {
    return new LinkBuilder(scmPathInfoStore.get(), ConfigurationResource.class);
  }

}
