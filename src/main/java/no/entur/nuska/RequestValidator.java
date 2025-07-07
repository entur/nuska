package no.entur.nuska;

import java.util.regex.Pattern;

/**
 * Validate the user-provided codespace and import key.
 */
public class RequestValidator {

  private static final Pattern CODESPACE_PATTERN = Pattern.compile(
    "^[a-z]{3}$"
  );
  private static final Pattern IMPORT_KEY_PATTERN = Pattern.compile(
    "^[a-z]{3}_\\d{4}-\\d{2}-\\d{2}T\\d{2}_\\d{2}_\\d{2}(?:\\.\\d{1,3})?$"
  );

  private final String codespace;
  private final String importKey;

  public RequestValidator(String codespace, String importKey) {
    this.codespace = codespace;
    this.importKey = importKey;
  }

  public RequestValidator(String codespace) {
    this(codespace, null);
  }

  public void validate() {
    validateCodespace(codespace);
    validateImportKey(importKey);
  }

  private void validateCodespace(String codespace) {
    if (codespace == null) {
      throw new BadRequestException("No codespace provided");
    }
    if (!CODESPACE_PATTERN.matcher(codespace).matches()) {
      throw new BadRequestException("Invalid codespace");
    }
  }

  private void validateImportKey(String importKey) {
    if (importKey == null) {
      return;
    }
    if (!IMPORT_KEY_PATTERN.matcher(importKey).matches()) {
      throw new BadRequestException("Invalid importKey");
    }
    if (!importKey.startsWith(codespace)) {
      throw new BadRequestException("Invalid importKey");
    }
  }
}
