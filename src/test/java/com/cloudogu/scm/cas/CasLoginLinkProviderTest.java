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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CasLoginLinkProviderTest {

  @Mock(answer = RETURNS_DEEP_STUBS)
  private CasContext context;

  @Test
  void createLoginLink() {
    when(context.get().getCasUrl()).thenReturn("https://cas.hitchhiker.com");
    final String loginLink = CasLoginLinkProvider.createLoginLink(context, "http://hitchhiker.com/scm/api/v2/cas/auth/v2:fSF5L2EOJqNoSdd8-KdqoUgujB6KWPQw8W9MAnjewWaS");
    Assertions.assertThat(loginLink).startsWith("https://cas.hitchhiker.com/login?service=http%3A%2F%2Fhitchhiker.com%2Fscm%2Fapi%2Fv2%2Fcas%2Fauth%2Fv2%3AfSF5L2EOJqNoSdd8-KdqoUgujB6KWPQw8W9MAnjewWaS");
  }
}
