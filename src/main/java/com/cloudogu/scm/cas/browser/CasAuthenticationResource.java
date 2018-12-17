package com.cloudogu.scm.cas.browser;

import sonia.scm.security.AllowAnonymousAccess;
import sonia.scm.security.CipherHandler;
import sonia.scm.util.HttpUtil;

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

@AllowAnonymousAccess
@Path(CasAuthenticationResource.PATH)
public class CasAuthenticationResource {

  public static final String PATH = "v2/cas";

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
  public Response login(
    @Context HttpServletRequest request,
    @Context HttpServletResponse response,
    @PathParam("urlSuffix") String encryptedUrlSuffix,
    @QueryParam("ticket") String ticket
  ) {
    String url = createRedirectUrl(request, encryptedUrlSuffix);
    CasToken token = CasToken.valueOf(ticket, encryptedUrlSuffix);

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
  public Response logout(@FormParam("logoutRequest") String logoutRequest) {
    logoutHandler.logout(logoutRequest);
    return Response.ok().build();
  }

}
