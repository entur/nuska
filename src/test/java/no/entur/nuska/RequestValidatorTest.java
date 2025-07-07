package no.entur.nuska;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RequestValidatorTest {

  @Test
  void testValidCodespaceWithoutImportKey() {
    assertDoesNotThrow(() -> new RequestValidator("aaa", null).validate());
  }

  @Test
  void testInvalidCodespaceWithoutImportKey() {
    RequestValidator validator = new RequestValidator("aaaa", null);
    assertThrows(BadRequestException.class, validator::validate);
  }

  @Test
  void testValidCodespaceWithValidImportKey() {
    RequestValidator validator = new RequestValidator(
      "aaa",
      "aaa_2023-08-25T12_23_25.429"
    );
    assertDoesNotThrow(validator::validate);
  }

  @Test
  void testValidCodespaceWithValidImportKeyMillisecondsIncomplete() {
    RequestValidator validator = new RequestValidator(
      "aaa",
      "aaa_2023-08-25T12_23_25.42"
    );
    assertDoesNotThrow(validator::validate);
  }

  @Test
  void testValidCodespaceWithValidImportKeyNoMilliseconds() {
    RequestValidator validator = new RequestValidator(
      "aaa",
      "aaa_2023-08-25T12_23_25"
    );
    assertDoesNotThrow(validator::validate);
  }

  @Test
  void testValidCodespaceWithInvalidImportKey() {
    RequestValidator validator = new RequestValidator(
      "aaa",
      "../bbb/bbb_2023-08-25T12_23_25.429"
    );
    assertThrows(BadRequestException.class, validator::validate);
  }
}
