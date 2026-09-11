package no.entur.nuska.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import no.entur.nuska.model.DatasetImport;
import no.entur.nuska.model.NetexDsjVariant;
import no.entur.nuska.repository.NuskaLocalDiskBlobStoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ByteArrayResource;

class NisabaBlobStoreServiceTest {

  private static final String CONTAINER_NAME = "nisaba-exchange";
  private static final String CODESPACE = "abc";
  private static final String IMPORT_KEY = "abc_2024-01-15T10_30_00.000";

  @TempDir
  private Path baseFolder;

  private NuskaLocalDiskBlobStoreRepository repository;
  private NisabaBlobStoreService service;

  @BeforeEach
  void setUp() {
    repository = new NuskaLocalDiskBlobStoreRepository(baseFolder.toString());
    service = new NisabaBlobStoreService(CONTAINER_NAME, repository);
  }

  @Test
  void testGetBlobInDefaultFolder() {
    uploadDataset(NetexDsjVariant.DEFAULT);

    assertEquals(
      content(NetexDsjVariant.DEFAULT),
      contentOf(service.getBlob(CODESPACE, IMPORT_KEY, NetexDsjVariant.DEFAULT))
    );
  }

  @Test
  void testGetBlobInVariantFolder() {
    uploadDataset(NetexDsjVariant.DEFAULT);
    uploadDataset(NetexDsjVariant.LEGACY);
    uploadDataset(NetexDsjVariant.NEW);

    assertEquals(
      content(NetexDsjVariant.LEGACY),
      contentOf(service.getBlob(CODESPACE, IMPORT_KEY, NetexDsjVariant.LEGACY))
    );
    assertEquals(
      content(NetexDsjVariant.NEW),
      contentOf(service.getBlob(CODESPACE, IMPORT_KEY, NetexDsjVariant.NEW))
    );
  }

  @Test
  void testGetBlobFallsBackToDefaultFolder() {
    uploadDataset(NetexDsjVariant.DEFAULT);

    assertEquals(
      content(NetexDsjVariant.DEFAULT),
      contentOf(service.getBlob(CODESPACE, IMPORT_KEY, NetexDsjVariant.LEGACY))
    );
  }

  @Test
  void testGetUnknownBlob() {
    assertNull(service.getBlob(CODESPACE, IMPORT_KEY, NetexDsjVariant.DEFAULT));
    assertNull(service.getBlob(CODESPACE, IMPORT_KEY, NetexDsjVariant.NEW));
  }

  @Test
  void testGetLatestBlobInVariantFolder() {
    uploadDataset(NetexDsjVariant.DEFAULT);
    uploadDataset(NetexDsjVariant.LEGACY);

    assertEquals(
      content(NetexDsjVariant.LEGACY),
      contentOf(service.getLatestBlob(CODESPACE, NetexDsjVariant.LEGACY))
    );
  }

  @Test
  void testGetLatestBlobFallsBackToDefaultFolder() {
    uploadDataset(NetexDsjVariant.DEFAULT);

    assertEquals(
      content(NetexDsjVariant.DEFAULT),
      contentOf(service.getLatestBlob(CODESPACE, NetexDsjVariant.NEW))
    );
  }

  @Test
  void testGetUnknownLatestBlob() {
    assertNull(service.getLatestBlob(CODESPACE, NetexDsjVariant.LEGACY));
  }

  @Test
  void testGetImportListReadsTheDefaultFolder() {
    uploadDataset(NetexDsjVariant.DEFAULT);
    uploadDataset(NetexDsjVariant.LEGACY);

    List<DatasetImport> imports = service.getImportList(CODESPACE);
    assertEquals(1, imports.size());
    assertEquals(IMPORT_KEY, imports.getFirst().importKey());
  }

  private void uploadDataset(NetexDsjVariant variant) {
    repository.uploadBlob(
      variant.subPath() + CODESPACE + '/' + IMPORT_KEY + ".zip",
      new ByteArrayInputStream(
        content(variant).getBytes(StandardCharsets.UTF_8)
      )
    );
  }

  private static String content(NetexDsjVariant variant) {
    return "dataset in " + variant.subPath();
  }

  private static String contentOf(ByteArrayResource resource) {
    assertNotNull(resource);
    return new String(resource.getByteArray(), StandardCharsets.UTF_8);
  }
}
