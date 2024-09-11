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

package com.cloudogu.scm.cas.browser;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilderFactory;
import sonia.scm.security.AllowAnonymousAccess;
import sonia.scm.security.CipherHandler;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.VndMediaType;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;

@OpenAPIDefinition(tags = {
  @Tag(name = "CAS Plugin", description = "CAS plugin provided endpoints")
})
@AllowAnonymousAccess
@Path(CasAuthenticationResource.PATH)
public class CasAuthenticationResource {

  private static final Logger LOG = LoggerFactory.getLogger(CasAuthenticationResource.class);

  public static final String PATH = "v2/cas/auth";

  private final LoginHandler loginHandler;
  private final LogoutHandler logoutHandler;
  private final CipherHandler cipherHandler;
  private final AccessTokenBuilderFactory accessTokenBuilderFactory;

  @Inject
  public CasAuthenticationResource(LoginHandler loginHandler, LogoutHandler logoutHandler, CipherHandler cipherHandler, AccessTokenBuilderFactory accessTokenBuilderFactory) {
    this.loginHandler = loginHandler;
    this.logoutHandler = logoutHandler;
    this.cipherHandler = cipherHandler;
    this.accessTokenBuilderFactory = accessTokenBuilderFactory;
  }

  @GET
  @Path("{urlSuffix}")
  @Operation(summary = "CAS login", description = "Login with CAS.", tags = "CAS Plugin")
  @ApiResponse(responseCode = "200", description = "success")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response login(
    @Context HttpServletRequest request,
    @Context HttpServletResponse response,
    @PathParam("urlSuffix") String encryptedUrlSuffix,
    @QueryParam("ticket") String ticket
  ) {
    String url = createRedirectUrl(request, encryptedUrlSuffix);
    CasToken token = CasToken.valueOf(ticket, encryptedUrlSuffix);
    LOG.debug("got login call from cas with cas token {}", token.getCredentials());

    loginHandler.login(request, response, token);

    return Response.seeOther(URI.create(url)).build();
  }

  private String createRedirectUrl(HttpServletRequest request, String encryptedUrlSuffix) {
    String redirectUrl = cipherHandler.decode(encryptedUrlSuffix);
    return HttpUtil.getCompleteUrl(request, redirectUrl);
  }

  @POST
  @Path("{urlSuffix}")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Operation(summary = "CAS logout", description = "Logout from CAS.", tags = "CAS Plugin")
  @ApiResponse(responseCode = "200", description = "success")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response logout(@FormParam("logoutRequest") String logoutRequest) {
    LOG.debug("got logout call from cas");
    logoutHandler.logout(logoutRequest);
    return Response.ok().build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Path("")
  @Operation(summary = "CAS access token", description = "Creates access token for SCM-Manager.", tags = "CAS Plugin")
  @ApiResponse(responseCode = "200", description = "success")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response accessToken(
    @Context HttpServletRequest request,
    @Context HttpServletResponse response,
    @FormParam("ticket") String ticket
  ) {
    CasToken token = CasToken.valueOf(ticket, "/");
    LOG.debug("got login call from cas with cas token {}", token.getCredentials());

    loginHandler.login(request, response, token);

    AccessToken accessToken = accessTokenBuilderFactory.create().build();
    return Response.ok().entity(accessToken.compact()).build();
  }

}
