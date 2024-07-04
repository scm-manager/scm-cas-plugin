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
package com.cloudogu.scm.cas;

import com.google.common.collect.ImmutableSet;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.user.User;
import sonia.scm.util.ValidationUtil;

import jakarta.inject.Inject;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class AssertionMapper {

  private static final Logger LOG = LoggerFactory.getLogger(AssertionMapper.class);

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
    setEmail(principal, attributes, user);
    user.setExternal(true);

    return user;
  }

  private void setEmail(AttributePrincipal principal, Map<String, Object> attributes, User user) {
    String mail = getMail(attributes);
    if (ValidationUtil.isMailAddressValid(mail)) {
      user.setMail(mail);
    } else {
      LOG.info("found invalid email address '{}' for cas user '{}'; leaving email blank", mail, principal.getName());
    }
  }

  private String getDisplayName(Map<String, Object> attributes, AttributePrincipal principal) {
    String displayName = getStringAttribute(attributes, context.get().getDisplayNameAttribute());
    return displayName != null ? displayName : principal.getName();
  }

  private String getMail(Map<String, Object> attributes) {
    return getStringAttribute(attributes, context.get().getMailAttribute());
  }

  private String getStringAttribute(Map<String, Object> attributes, String attributeName) {

    if (attributes.get(attributeName) instanceof Iterable) {
      Iterable iterable = (Iterable) attributes.get(attributeName);
      return iterable.iterator().next().toString();
    }
    Object attributeValue = attributes.get(attributeName);
    if (attributeValue != null) {
      return attributeValue.toString();
    }
    return null;
  }

  public Set<String> createGroups(Assertion assertion) {
    Map<String, Object> attributes = assertion.getPrincipal().getAttributes();

    ImmutableSet.Builder<String> builder = ImmutableSet.builder();

    Object attribute = attributes.get(context.get().getGroupAttribute());
    if (attribute instanceof Collection) {
      for (Object item : (Collection) attribute) {
        builder.add(item.toString());
      }
    } else if (attribute != null) {
      builder.add(attribute.toString());
    }

    return builder.build();
  }
}
