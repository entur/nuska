package no.entur.nuska;

import java.io.IOException;

public class NuskaException extends RuntimeException {

  public NuskaException(String message) {
    super(message);
  }

  public NuskaException(String message, Throwable cause) {
    super(message, cause);
  }

  public NuskaException(IOException e) {
    super(e);
  }
}
