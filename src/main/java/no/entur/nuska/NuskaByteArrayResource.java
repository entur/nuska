package no.entur.nuska;

import org.springframework.core.io.ByteArrayResource;

public class NuskaByteArrayResource extends ByteArrayResource {

  private final String filename;

  public NuskaByteArrayResource(byte[] byteArray, String filename) {
    super(byteArray, null);
    this.filename = filename;
  }

  @Override
  public String getFilename() {
    return filename;
  }
}
