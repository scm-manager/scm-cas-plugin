package com.cloudogu.scm.cas;

import sonia.scm.group.GroupResolver;
import sonia.scm.plugin.Extension;

import javax.inject.Inject;
import java.util.Set;

@Extension
public class CasGroupResolver implements GroupResolver {

  private GroupStore store;

  @Inject
  public CasGroupResolver(GroupStore store) {
    this.store = store;
  }

  @Override
  public Set<String> resolve(String principal) {
    return store.get(principal);
  }
}
