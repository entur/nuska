package no.entur.nuska;

import java.io.InputStream;
import java.security.Principal;
import java.util.concurrent.atomic.AtomicLong;
import no.entur.nuska.security.NuskaAuthorizationService;
import no.entur.nuska.service.NisabaBlobStoreService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class NuskaController {

  private static final String template = "Hello, %s!";
  private final AtomicLong counter = new AtomicLong();

  private final NuskaAuthorizationService authorizationService;
  private final NisabaBlobStoreService blobStoreService;

  public NuskaController(
    NuskaAuthorizationService authorizationService,
    NisabaBlobStoreService blobStoreService
  ) {
    this.authorizationService = authorizationService;
    this.blobStoreService = blobStoreService;
  }

  @GetMapping("/greeting")
  public String greeting(
    @RequestParam(value = "name", defaultValue = "World") String name
  ) {
    return counter.incrementAndGet() + " " + String.format(template, name);
  }

  @GetMapping("download_netex/{codespace}")
  public ResponseEntity<Resource> downloadNetex(
    @PathVariable(value = "codespace") String codespace,
    Principal principal
  ) {
    if (codespace == null) {
      throw new NuskaException("No codespace provided");
    }

    try {
      authorizationService.verifyBlockViewerPrivileges(codespace);
      InputStream latestBlob = blobStoreService.getLatestBlob(codespace);
      InputStreamResource resource = new InputStreamResource(latestBlob);
      return ResponseEntity
        .ok()
        .contentLength(latestBlob.readAllBytes().length)
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .body(resource);
    } catch (Exception e) {
      throw new NuskaException(
        "No block viewer privileges for user " + principal.getName()
      );
    }
  }
}
