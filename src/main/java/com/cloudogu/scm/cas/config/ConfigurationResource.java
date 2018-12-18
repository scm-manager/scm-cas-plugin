package com.cloudogu.scm.cas.config;

import com.cloudogu.scm.cas.CasContext;
import com.cloudogu.scm.cas.Configuration;
import com.cloudogu.scm.cas.Constants;
import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path(ConfigurationResource.PATH)
public class ConfigurationResource {

  private static final String CONTENT_TYPE =  VndMediaType.PREFIX + "casConfig" + VndMediaType.SUFFIX;

  static final String PATH = "v2/cas/configuration";

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
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"configuration:read:cas\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  public ConfigurationDto get() {
    ConfigurationPermissions.read(Constants.NAME).check();
    Configuration configuration = context.get();
    return mapper.toDto(configuration);
  }

  @PUT
  @Path("")
  @Consumes(CONTENT_TYPE)
  @StatusCodes({
    @ResponseCode(code = 204, condition = "update success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user does not have the \"configuration:write:cas\" privilege"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @TypeHint(TypeHint.NO_CONTENT.class)
  public Response update(ConfigurationDto dto) {
    ConfigurationPermissions.write(Constants.NAME).check();
    Configuration configuration = mapper.fromDto(dto);
    context.set(configuration);
    return Response.noContent().build();
  }
}
