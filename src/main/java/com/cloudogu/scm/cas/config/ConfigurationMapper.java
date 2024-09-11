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

package com.cloudogu.scm.cas.config;

import com.cloudogu.scm.cas.Configuration;
import com.cloudogu.scm.cas.Constants;
import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ConfigurationPermissions;

import jakarta.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class ConfigurationMapper {

  abstract Configuration fromDto(ConfigurationDto dto);

  @Mapping(
    target = "attributes",
    ignore = true
  )
  abstract ConfigurationDto toDto(Configuration configuration);

  @Inject
  private ScmPathInfoStore scmPathInfoStore;

  @AfterMapping
  void appendLinks(@MappingTarget ConfigurationDto dto) {
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
