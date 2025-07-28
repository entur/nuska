package no.entur.nuska;

import java.util.List;
import no.entur.nuska.security.NuskaAuthorizationService;
import no.entur.nuska.service.NetexImport;
import no.entur.nuska.service.NisabaBlobStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class NuskaController {

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

  /**
   * @deprecated Use {@link #getDataset(String, String, String)} for an arbitrary version
   * or {@link #getLatestDataset(String, String)} for the latest version.
   */
  @Deprecated
  @GetMapping(value = "timetable-data/{codespace}")
  public ResponseEntity<Resource> downloadTimetableData(
    @PathVariable(value = "codespace") String codespace,
    @RequestParam(name = "importKey", required = false) String importKey,
    @RequestHeader(value = "Accept", required = false) String acceptHeader
  ) {
    return downloadDataset(codespace, importKey, acceptHeader);
  }

  @GetMapping(
    value = "timetable-data/datasets/{codespace}/version/{importKey}",
    produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
  )
  public ResponseEntity<Resource> getDataset(
    @PathVariable(value = "codespace") String codespace,
    @PathVariable(name = "importKey", required = false) String importKey,
    @RequestHeader(value = "Accept", required = false) String acceptHeader
  ) {
    return downloadDataset(codespace, importKey, acceptHeader);
  }

  @GetMapping(
    value = "timetable-data/datasets/{codespace}/latest",
    produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
  )
  public ResponseEntity<Resource> getLatestDataset(
    @PathVariable(value = "codespace") String codespace,
    @RequestHeader(value = "Accept", required = false) String acceptHeader
  ) {
    return downloadDataset(codespace, null, acceptHeader);
  }

  @GetMapping(
    value = " timetable-data/datasets/{codespace}/versions",
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<List<NetexImport>> getDatasetVersions(
    @PathVariable(value = "codespace") String codespace
  ) {
    new RequestValidator(codespace).validate();

    LOGGER.info(
      "Received request to list timetable data import for codespace '{}'",
      codespace
    );

    try {
      authorizationService.verifyBlockViewerPrivileges(codespace);
      List<NetexImport> imports = blobStoreService.getImportList(codespace);
      if (!imports.isEmpty()) {
        return ResponseEntity.ok().body(imports);
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
