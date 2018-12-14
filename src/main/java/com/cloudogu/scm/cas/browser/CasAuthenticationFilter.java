package com.cloudogu.scm.cas.browser;

import sonia.scm.Priority;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.filter.Filters;
import sonia.scm.filter.WebElement;
import sonia.scm.web.WebTokenGenerator;
import sonia.scm.web.filter.AuthenticationFilter;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

@WebElement("/*")
@Priority(Filters.PRIORITY_AUTHENTICATION)
public class CasAuthenticationFilter extends AuthenticationFilter {

  @Inject
  public CasAuthenticationFilter(ScmConfiguration configuration, Set<WebTokenGenerator> tokenGenerators) {
    super(configuration, tokenGenerators);
  }

  @Override
  protected void handleUnauthorized(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    chain.doFilter(request, response);
  }
}
