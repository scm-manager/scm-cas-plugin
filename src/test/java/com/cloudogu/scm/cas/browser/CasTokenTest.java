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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CasTokenTest {

  @Test
  void shouldReturnTheGivenTicketAndUrlSuffix() {
    CasToken token = CasToken.valueOf("TGT-123", "__enc__");
    assertThat(token.getCredentials()).isEqualTo("TGT-123");
    assertThat(token.getUrlSuffix()).isEqualTo("__enc__");
  }

  @Test
  void shouldThrowUnsupportedOperationExceptionOnGetPrincipal() {
    CasToken token = CasToken.valueOf("TGT-123", "__enc__");
    assertThrows(UnsupportedOperationException.class, () -> token.getPrincipal());
  }

  @Test
  void shouldFailIfTicketIsNull() {
    assertThrows(IllegalArgumentException.class, () -> CasToken.valueOf(null, "__enc__"));
  }

  @Test
  void shouldFailIfTicketIsEmpty() {
    assertThrows(IllegalArgumentException.class, () -> CasToken.valueOf("", "__enc__"));
  }

  @Test
  void shouldFailIfUrlSuffixIsNull() {
    assertThrows(IllegalArgumentException.class, () -> CasToken.valueOf("TGT-123", null));
  }

  @Test
  void shouldFailIfUrlSuffixIsEmpty() {
    assertThrows(IllegalArgumentException.class, () -> CasToken.valueOf("TGT-123", ""));
  }

}
