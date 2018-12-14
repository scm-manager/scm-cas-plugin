package com.cloudogu.scm.cas;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
