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
import sonia.scm.security.AllowAnonymousAccess;
import sonia.scm.security.CipherHandler;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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

  @Inject
  public CasAuthenticationResource(LoginHandler loginHandler, LogoutHandler logoutHandler, CipherHandler cipherHandler) {
    this.loginHandler = loginHandler;
    this.logoutHandler = logoutHandler;
    this.cipherHandler = cipherHandler;
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

}
