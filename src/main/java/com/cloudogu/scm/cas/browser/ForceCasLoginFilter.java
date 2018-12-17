package com.cloudogu.scm.cas.browser;

import com.cloudogu.scm.cas.Configuration;
import com.cloudogu.scm.cas.ServiceUrlProvider;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import sonia.scm.Priority;
import sonia.scm.filter.Filters;
import sonia.scm.filter.WebElement;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.filter.HttpFilter;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebElement("/*")
@Priority(Filters.PRIORITY_POST_AUTHENTICATION)
public class ForceCasLoginFilter extends HttpFilter {

  private final ServiceUrlProvider serviceUrlProvider;
  private final Configuration configuration;

  @Inject
  public ForceCasLoginFilter(ServiceUrlProvider serviceUrlProvider, Configuration configuration) {
    this.serviceUrlProvider = serviceUrlProvider;
    this.configuration = configuration;
  }

  @Override
  protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    Subject subject = SecurityUtils.getSubject();
    if (subject.isAuthenticated() || isCasCallback(request)) {
      chain.doFilter(request, response);
    } else {
      response.sendRedirect(createCasLoginRedirect());
    }
  }

  private boolean isCasCallback(HttpServletRequest request) {
    return request.getRequestURI().contains("/v2/cas/");
  }

  private String createCasLoginRedirect() {
    String encodedServiceUrl = HttpUtil.encode(serviceUrlProvider.create());
    return configuration.getCasLoginUrl() + "?service=" + encodedServiceUrl;
  }

}
