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

package com.cloudogu.scm.cas;

import sonia.scm.group.GroupResolver;
import sonia.scm.plugin.Extension;

import jakarta.inject.Inject;
import java.util.Set;

import static java.util.Collections.emptySet;

@Extension
public class CasGroupResolver implements GroupResolver {

  private GroupStore store;

  @Inject
  public CasGroupResolver(GroupStore store) {
    this.store = store;
  }

  @Override
  public Set<String> resolve(String principal) {
    Set<String> groups = store.get(principal);
    if (groups == null) {
      return emptySet();
    }
    return groups;
  }
}
