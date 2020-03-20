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
