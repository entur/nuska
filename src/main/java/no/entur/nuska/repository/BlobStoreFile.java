package no.entur.nuska.repository;

import java.time.Instant;

/**
 * A file in the blobstore, identified by its name and creation date.
 */
public class BlobStoreFile {

  private final String name;
  private final Instant creationDate;

  public BlobStoreFile(String name, Instant creationDate) {
    this.name = name;
    this.creationDate = creationDate;
  }

  public String name() {
    return name;
  }

  public Instant creationDate() {
    return creationDate;
  }
}
