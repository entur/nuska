package no.entur.nuska.service;

import java.time.Instant;

/**
 * A single dataset import, identified by its creation date and the import key recorded by Nisaba.
 */
public record NetexImport(String importKey, Instant creationDate) {}
