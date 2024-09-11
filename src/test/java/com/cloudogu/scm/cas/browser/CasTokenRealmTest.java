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

package com.cloudogu.scm.cas.browser;

import com.cloudogu.scm.cas.AuthenticationInfoBuilder;
import com.cloudogu.scm.cas.ServiceUrlProvider;
import org.apache.shiro.authc.AuthenticationInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CasTokenRealmTest {

  private static final String SERVICE_URL = "https://scm.hitchhiker.com/scm";

  @Mock
  private ServiceUrlProvider serviceUrlProvider;

  @Mock
  private AuthenticationInfoBuilder authenticationInfoBuilder;

  @InjectMocks
  private CasTokenRealm realm;

  @Mock
  private AuthenticationInfo authenticationInfo;

  @Test
  void shouldReturnAuthenticationInfo() {
    CasToken token = CasToken.valueOf("TGT-123", "anc");

    when(serviceUrlProvider.createFromToken(token)).thenReturn(SERVICE_URL);
    when(authenticationInfoBuilder.create("TGT-123", SERVICE_URL)).thenReturn(authenticationInfo);

    AuthenticationInfo result = realm.doGetAuthenticationInfo(token);
    assertThat(result).isSameAs(authenticationInfo);
  }

}
