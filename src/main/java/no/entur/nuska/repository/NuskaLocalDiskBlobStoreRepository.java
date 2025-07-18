package no.entur.nuska.repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import no.entur.nuska.NuskaByteArrayResource;
import no.entur.nuska.NuskaException;
import org.rutebanken.helper.storage.repository.LocalDiskBlobStoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;

public class NuskaLocalDiskBlobStoreRepository
  extends LocalDiskBlobStoreRepository
  implements NuskaBlobStoreRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(
    NuskaLocalDiskBlobStoreRepository.class
  );

  public NuskaLocalDiskBlobStoreRepository(String baseFolder) {
    super(baseFolder);
  }

  @Override
  public ByteArrayResource getLatestBlob(String path) {
    LOGGER.debug("get latest blob called in local-disk blob");

    File dir = new File(getContainerFolder(), path);
    if (dir.isDirectory()) {
      Optional<File> opFile = Arrays
        .stream(Objects.requireNonNull(dir.listFiles(File::isFile)))
        .filter(this::isValidFile)
        .max(Comparator.comparingLong(File::lastModified));

      if (opFile.isPresent()) {
        try {
          return new NuskaByteArrayResource(
            getBlob(opFile.get().getAbsolutePath()).readAllBytes(),
            opFile.get().getName()
          );
        } catch (IOException e) {
          throw new NuskaException(e);
        }
      }
    }
    return null;
  }

  @Override
  public List<BlobStoreFile> listBlobs(String prefix) {
    if (Paths.get(getContainerFolder(), prefix).toFile().isDirectory()) {
      try (
        Stream<Path> walk = Files.walk(Paths.get(getContainerFolder(), prefix))
      ) {
        return walk
          .filter(Files::isRegularFile)
          .map(path ->
            new BlobStoreFile(
              Paths.get(getContainerFolder()).relativize(path).toString(),
              getFileCreationDate(path)
            )
          )
          .sorted(Comparator.comparing(BlobStoreFile::creationDate))
          .toList();
      } catch (IOException e) {
        throw new NuskaException(e);
      }
    } else {
      return List.of();
    }
  }

  private boolean isValidFile(File file) {
    try {
      Path path = file.toPath();
      return Files.isRegularFile(path) && !Files.isHidden(path);
    } catch (IOException e) {
      return false;
    }
  }

  private static Instant getFileCreationDate(Path path) {
    try {
      BasicFileAttributes attr = Files.readAttributes(
        path,
        BasicFileAttributes.class
      );
      return Instant.ofEpochMilli(attr.creationTime().toMillis());
    } catch (IOException e) {
      throw new NuskaException(e);
    }
  }
}
