# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Nuska is a Spring Boot 3 / Java 21 REST service (`no.entur:nuska`) that gives authenticated access to the original NeTEx datasets uploaded to Entur's `nisaba-exchange` GCS bucket. It inherits from `org.entur.ror:superpom` and depends on the Entur RoR helpers (`storage-gcp-gcs`, `oauth2`, `permission-store-proxy`).

## Build, test, run

```bash
mvn verify                                          # full CI build (what push.yml runs)
mvn test                                            # unit tests only
mvn -Dtest=RequestValidatorTest test                # single test class
mvn -Dtest=RequestValidatorTest#testValidCodespaceWithoutImportKey test   # single method
mvn spring-boot:run                                 # run locally
mvn prettier:write                                  # format Java sources
```

- `openapi-generator-maven-plugin` regenerates the `TimetableDataApi` interface from `src/main/resources/openapi/openapi.yaml` into `target/generated-sources` (package `no.entur.nuska.rest.openapi.{api,model}`) on every build — edit the YAML, not the generated code.
- `prettier-maven-plugin` runs during `validate` with goal `write` by default (formats in place). The `prettierCheck` profile flips it to `check` (used in CI). The `release` and `sonar` profiles skip prettier entirely.

## Spring profiles (important — nothing works without one of these)

There is **no `application.properties` in `src/main/resources`** — runtime config is supplied by the Helm ConfigMap (`helm/nuska/templates/configmap.yaml`) or the test properties file. At least one blob-store profile must be active:

- `gcs-blobstore` — activates `GcsBlobStoreConfig`, wiring `NuskaGcsBlobStoreRepository` (used in all deployed envs).
- `local-disk-blobstore` — activates `LocalBlobStoreConfig`, wiring `NuskaLocalDiskBlobStoreRepository` from `blobstore.local.folder` (used for tests and local dev).
- `test` — disables the `OAuth2Config` and `NuskaWebSecurityConfiguration` beans (both are annotated `@Profile("!test")`), so tests do not need a JWT issuer.

The role-assignment extractor is selected at runtime by `nuska.security.role.assignment.extractor` (`jwt` — default, decodes roles from the JWT — or `baba` — calls the remote Baba permission service via an OAuth2 client-credentials `WebClient`). See `security/AuthorizationConfig.java`.

## Request flow

1. `NuskaController` implements the generated `TimetableDataApi`. It is annotated `@Service` (not `@RestController`) because the Spring-generated interface already carries `@RequestMapping`; the controller adds one extra `@GetMapping` for `/timetable-data/openapi.yaml` to serve the spec itself.
2. Every handler first constructs `RequestValidator(codespace, importKey)` and calls `validate()`. Codespace must match `^[a-z]{3}$`; import keys must match `^[a-z]{3}_\d{4}-\d{2}-\d{2}T\d{2}_\d{2}_\d{2}(?:\.\d{1,3})?$` **and** start with the codespace. Validation failure throws `BadRequestException` — do not bypass it.
3. `NuskaAuthorizationService.verifyBlockViewerPrivileges(codespace)` delegates to `DefaultAuthorizationService.validateViewBlockData` from the RoR helpers. The codespace is upper-cased before lookup (see the lambda in `AuthorizationConfig#tokenBasedAuthorizationService`).
4. `NisabaBlobStoreService` prefixes every path with `imported/`, appends `.zip`, and caps version listings at `MAX_NUM_IMPORT = 10` (the 10 most recent, sorted ascending by creation date in the response).
5. Controller catches `AccessDeniedException` and rethrows with a safe message; all other exceptions are logged and rethrown as `NuskaException` so stack traces never reach the client.

## Security config

`NuskaWebSecurityConfiguration` permits only `/timetable-data/openapi.yaml`, `/actuator/prometheus`, `/actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness` without auth; everything else goes through `MultiIssuerAuthenticationManagerResolver` (Entur internal + partner Auth0 tenants, configured via `nuska.oauth2.resourceserver.auth0.entur.{internal,partner}.jwt.*`). CORS is wide open (`*` origin). Both this class and `OAuth2Config` are `@Profile("!test")`.

## Deployment

Helm chart lives in `helm/nuska/`; per-env overrides are in `helm/nuska/env/values-kub-ent-{dev,tst,prd}.yaml`. The Dockerfile uses Spring Boot layered jars extracted by `java -Djarmode=tools ... extract --layers` on top of `bellsoft/liberica-openjre-alpine:21.0.10`. CI (`.github/workflows/push.yml`) runs `mvn verify`, uploads the jar, runs SonarCloud, lints/publishes the OpenAPI spec via `entur/gha-api`, then builds and pushes the Docker image via `entur/gha-docker`.
