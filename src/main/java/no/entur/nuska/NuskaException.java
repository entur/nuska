package no.entur.nuska;

public class NuskaException extends RuntimeException {

  public NuskaException(String message) {
    super(message);
  }

  public NuskaException(String message, Throwable cause) {
    super(message, cause);
  }

  public NuskaException(Exception e) {
    super(e);
  }
}
