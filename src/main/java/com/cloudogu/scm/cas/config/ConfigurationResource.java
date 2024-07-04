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
