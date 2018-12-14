package com.cloudogu.scm.cas.browser;

import org.apache.shiro.SecurityUtils;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilder;
import sonia.scm.security.AccessTokenBuilderFactory;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.security.AllowAnonymousAccess;
import sonia.scm.security.CipherHandler;
import sonia.scm.util.HttpUtil;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URI;

@AllowAnonymousAccess
@Path(CasAuthenticationResource.PATH)
public class CasAuthenticationResource {

  public static final String PATH = "v2/cas";

  private final AccessTokenBuilderFactory tokenBuilderFactory;
  private final AccessTokenCookieIssuer cookieIssuer;
  private final CipherHandler cipherHandler;

  @Inject
  public CasAuthenticationResource(AccessTokenBuilderFactory tokenBuilderFactory, AccessTokenCookieIssuer cookieIssuer, CipherHandler cipherHandler) {
    this.tokenBuilderFactory = tokenBuilderFactory;
    this.cookieIssuer = cookieIssuer;
    this.cipherHandler = cipherHandler;
  }

  @GET
  @Path("{urlSuffix}")
  public Response authenticate(
    @Context HttpServletRequest request,
    @Context HttpServletResponse response,
    @PathParam("urlSuffix") String encryptedUrlSuffix,
    @QueryParam("ticket") String ticket
  ) {

    String url = createRedirectUrl(request, encryptedUrlSuffix);

    CasToken token = CasToken.valueOf(ticket, encryptedUrlSuffix);
    authenticate(request, response, token);

    return Response.seeOther(URI.create(url)).build();
  }

  private void authenticate(HttpServletRequest request, HttpServletResponse response, CasToken token) {
    SecurityUtils.getSubject().login(token);

    AccessTokenBuilder accessTokenBuilder = tokenBuilderFactory.create();
    AccessToken accessToken = accessTokenBuilder.build();

    cookieIssuer.authenticate(request, response, accessToken);
  }

  private String createRedirectUrl(HttpServletRequest request, String encryptedUrlSuffix) {
    String redirectUrl = cipherHandler.decode(encryptedUrlSuffix);
    return HttpUtil.getCompleteUrl(request, redirectUrl);
  }

}
