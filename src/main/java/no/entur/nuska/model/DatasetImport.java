package no.entur.nuska.model;

import java.time.Instant;

/**
 * A single dataset import, identified by its creation date and the import key recorded by Nisaba.
 */
public record DatasetImport(String importKey, Instant creationDate) {}
