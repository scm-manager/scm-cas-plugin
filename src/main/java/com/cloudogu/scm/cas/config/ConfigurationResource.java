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

import com.cloudogu.scm.cas.CasContext;
import com.cloudogu.scm.cas.Configuration;
import com.cloudogu.scm.cas.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.web.VndMediaType;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path(ConfigurationResource.PATH)
public class ConfigurationResource {

  private static final String CONTENT_TYPE = VndMediaType.PREFIX + "casConfig" + VndMediaType.SUFFIX;

  public static final String PATH = "v2/cas/configuration";

  private final CasContext context;
  private final ConfigurationMapper mapper;

  @Inject
  public ConfigurationResource(CasContext context, ConfigurationMapper mapper) {
    this.context = context;
    this.mapper = mapper;
  }

  @GET
  @Path("")
  @Produces(CONTENT_TYPE)
  @Operation(summary = "Get cas configuration", description = "Returns the cas configuration.", tags = "CAS Plugin")
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = CONTENT_TYPE,
      schema = @Schema(implementation = ConfigurationDto.class)
    )
  )
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the \"configuration:read:cas\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public ConfigurationDto get() {
    ConfigurationPermissions.read(Constants.NAME).check();
    Configuration configuration = context.get();
    return mapper.toDto(configuration);
  }

  @PUT
  @Path("")
  @Consumes(CONTENT_TYPE)
  @Operation(summary = "Update cas configuration", description = "Modifies the cas configuration.", tags = "CAS Plugin")
  @ApiResponse(responseCode = "204", description = "update success")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized /  the current user does not have the \"configuration:write:cas\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response update(ConfigurationDto dto) {
    ConfigurationPermissions.write(Constants.NAME).check();
    Configuration configuration = mapper.fromDto(dto);
    context.set(configuration);
    return Response.noContent().build();
  }
}
