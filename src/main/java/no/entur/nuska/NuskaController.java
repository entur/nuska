package no.entur.nuska;

import java.time.ZoneOffset;
import java.util.List;
import no.entur.nuska.model.DatasetImport;
import no.entur.nuska.rest.openapi.api.TimetableDataApi;
import no.entur.nuska.rest.openapi.model.NetexImport;
import no.entur.nuska.security.NuskaAuthorizationService;
import no.entur.nuska.service.NisabaBlobStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

@Service
class NuskaController implements TimetableDataApi {

  private static final Logger LOGGER = LoggerFactory.getLogger(
    NuskaController.class
  );

  private final NuskaAuthorizationService authorizationService;
  private final NisabaBlobStoreService blobStoreService;

  public NuskaController(
    NuskaAuthorizationService authorizationService,
    NisabaBlobStoreService blobStoreService
  ) {
    this.authorizationService = authorizationService;
    this.blobStoreService = blobStoreService;
  }

  @GetMapping(
    value = "/timetable-data/openapi.json",
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<Resource> getOpenApiSpec() {
    ClassPathResource resource = new ClassPathResource("openapi/openapi.json");

    if (!resource.exists()) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(resource);
  }

  /**
   * @deprecated Use {@link #getDataset(String, String, String)} for an arbitrary version
   * or {@link #getLatestDataset(String, String)} for the latest version.
   */
  @Override
  @Deprecated
  public ResponseEntity<Resource> downloadTimetableData(
    String codespace,
    String importKey,
    String acceptHeader
  ) {
    return downloadDataset(codespace, importKey, acceptHeader);
  }

  @Override
  public ResponseEntity<Resource> getDataset(
    String codespace,
    String importKey,
    String acceptHeader
  ) {
    return downloadDataset(codespace, importKey, acceptHeader);
  }

  @Override
  public ResponseEntity<Resource> getLatestDataset(
    String codespace,
    String acceptHeader
  ) {
    return downloadDataset(codespace, null, acceptHeader);
  }

  @Override
  public ResponseEntity<List<NetexImport>> getDatasetVersions(
    String codespace
  ) {
    new RequestValidator(codespace).validate();

    LOGGER.info(
      "Received request to list timetable data import for codespace '{}'",
      codespace
    );

    try {
      authorizationService.verifyBlockViewerPrivileges(codespace);
      List<DatasetImport> imports = blobStoreService.getImportList(codespace);
      List<NetexImport> list = imports
        .stream()
        .map(NuskaController::mapNetexImport)
        .toList();
      if (!imports.isEmpty()) {
        return ResponseEntity.ok().body(list);
      } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      }
    } catch (AccessDeniedException e) {
      throw new AccessDeniedException("No block viewer privileges");
    } catch (Exception e) {
      LOGGER.error("Failed to download timetable data", e);
      // do not send stacktrace to the client
      throw new NuskaException("Failed to download timetable data");
    }
  }

  private static NetexImport mapNetexImport(DatasetImport datasetImport) {
    NetexImport netexImport = new NetexImport();
    netexImport.setImportKey(datasetImport.importKey());
    netexImport.setCreationDate(
      datasetImport.creationDate().atOffset(ZoneOffset.UTC)
    );
    return netexImport;
  }

  private ResponseEntity<Resource> downloadDataset(
    String codespace,
    String importKey,
    String acceptHeader
  ) {
    // TODO log Accept header for debugging. To be removed.
    if (acceptHeader != null) {
      LOGGER.info("Client accepted content types: {}", acceptHeader);
    } else {
      LOGGER.info("Client did not specify Accept header.");
    }

    new RequestValidator(codespace, importKey).validate();

    LOGGER.info(
      "Received request to download timetable data for codespace '{}'",
      codespace
    );

    try {
      authorizationService.verifyBlockViewerPrivileges(codespace);
      ByteArrayResource blob;
      if (importKey != null) {
        blob = blobStoreService.getBlob(codespace, importKey);
      } else {
        blob = blobStoreService.getLatestBlob(codespace);
      }
      if (blob != null) {
        if (importKey == null) {
          LOGGER.info(
            "Successfully downloaded timetable data for codespace '{}'",
            codespace
          );
        } else {
          LOGGER.info(
            "Successfully downloaded timetable data for codespace '{}' and import key '{}'",
            codespace,
            importKey
          );
        }

        return ResponseEntity
          .ok()
          .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + blob.getFilename() + "\""
          )
          .body(blob);
      } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      }
    } catch (AccessDeniedException e) {
      throw new AccessDeniedException("No block viewer privileges");
    } catch (Exception e) {
      LOGGER.error("Failed to download timetable data", e);
      // do not send stacktrace to the client
      throw new NuskaException("Failed to download timetable data");
    }
  }
}
