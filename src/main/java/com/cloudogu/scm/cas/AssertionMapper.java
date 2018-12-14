package com.cloudogu.scm.cas;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import sonia.scm.user.User;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class AssertionMapper {

  private final Configuration configuration;

  @Inject
  public AssertionMapper(Configuration configuration) {
    this.configuration = configuration;
  }

  public User createUser(Assertion assertion) {
    AttributePrincipal principal = assertion.getPrincipal();
    Map<String, Object> attributes = principal.getAttributes();

    User user = new User(principal.getName());

    user.setDisplayName(getDisplayName(attributes));
    user.setMail(getMail(attributes));

    user.setType("cas");

    return user;
  }

  private String getDisplayName(Map<String,Object> attributes) {
    return getStringAttribute(attributes, configuration.getDisplayNameAttribute());
  }

  private String getMail(Map<String, Object> attributes) {
    return getStringAttribute(attributes, configuration.getMailAttribute());
  }

  private String getStringAttribute(Map<String,Object> attributes, String attributeName) {
    Object attributeValue = attributes.get(attributeName);
    if (attributeValue != null) {
      return attributeValue.toString();
    }
    return null;
  }

  public Collection<String> createGroups(Assertion assertion) {
    return Collections.emptyList();
  }
}
