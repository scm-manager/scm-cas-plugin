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

import sonia.scm.util.HttpUtil;

public final class CasLoginLinkProvider {
  private CasLoginLinkProvider() {
  }

  public static String createLoginLink(CasContext casContext, String serviceUrl) {
    String encodedServiceUrl = HttpUtil.encode(serviceUrl);
    return HttpUtil.append(casContext.get().getCasUrl(), "login") + "?service=" + encodedServiceUrl;
  }
}
