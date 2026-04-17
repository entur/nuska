package no.entur.nuska;

public class NuskaException extends RuntimeException {

  public NuskaException(String message) {
    super(message);
  }

  public NuskaException(Exception e) {
    super(e);
  }
}
