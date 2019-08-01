package com.cloudogu.scm.cas;

import com.google.common.collect.ImmutableSet;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import sonia.scm.user.User;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

public class AssertionMapper {

  private final CasContext context;

  @Inject
  public AssertionMapper(CasContext context) {
    this.context = context;
  }

  public User createUser(Assertion assertion) {
    AttributePrincipal principal = assertion.getPrincipal();
    Map<String, Object> attributes = principal.getAttributes();

    User user = new User(principal.getName());

    user.setDisplayName(getDisplayName(attributes, principal));
    user.setMail(getMail(attributes));
    user.setType(Constants.NAME);

    return user;
  }

  private String getDisplayName(Map<String,Object> attributes, AttributePrincipal principal) {
    String displayName = getStringAttribute(attributes, context.get().getDisplayNameAttribute());
    return displayName != null ? displayName : principal.getName();
  }

  private String getMail(Map<String, Object> attributes) {
    return getStringAttribute(attributes, context.get().getMailAttribute());
  }

  private String getStringAttribute(Map<String,Object> attributes, String attributeName) {

    if(attributes.get(attributeName) instanceof Iterable) {
      Iterable iterable = (Iterable) attributes.get(attributeName);
      return iterable.iterator().next().toString();
    }
    Object attributeValue = attributes.get(attributeName);
    if (attributeValue != null) {
      return attributeValue.toString();
    }
    return null;
  }

  public Collection<String> createGroups(Assertion assertion) {
    Map<String, Object> attributes = assertion.getPrincipal().getAttributes();

    ImmutableSet.Builder<String> builder = ImmutableSet.builder();

    Object attribute = attributes.get(context.get().getGroupAttribute());
    if (attribute instanceof Collection) {
      for ( Object item : (Collection) attribute ) {
        builder.add(item.toString());
      }
    } else if (attribute != null) {
      builder.add(attribute.toString());
    }

    return builder.build();
  }
}
