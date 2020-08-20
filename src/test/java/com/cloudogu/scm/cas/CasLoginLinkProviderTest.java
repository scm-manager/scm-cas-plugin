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
