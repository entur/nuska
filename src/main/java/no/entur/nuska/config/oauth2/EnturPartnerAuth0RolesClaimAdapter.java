package no.entur.nuska.config.oauth2;

import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.IOException;
import java.util.*;
import no.entur.nuska.NuskaException;
import org.entur.oauth2.RoROAuth2Claims;
import org.rutebanken.helper.organisation.AuthorizationConstants;
import org.rutebanken.helper.organisation.RoleAssignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.MappedJwtClaimSetConverter;

/**
 * Insert a "role_assignments" claim in the JWT token based on the organisationID claim, for compatibility with the existing
 * authorization process (@{@link org.entur.oauth2.JwtRoleAssignmentExtractor}).
 * This custom mapping is intended for tokens sent from external client that do not contain the role_assignments nor the permissions claim.
 */
public class EnturPartnerAuth0RolesClaimAdapter
  implements Converter<Map<String, Object>, Map<String, Object>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(
    EnturPartnerAuth0RolesClaimAdapter.class
  );

  static final String ORG_RUTEBANKEN = "RB";
  static final String OPENID_AUDIENCE_CLAIM = "aud";
  static final String ORGANISATION_ID_CLAIM = "https://entur.io/organisationID";

  private static final ObjectWriter ROLE_ASSIGNMENT_OBJECT_WRITER =
    ObjectMapperFactory.getSharedObjectMapper().writerFor(RoleAssignment.class);

  private final MappedJwtClaimSetConverter delegate =
    MappedJwtClaimSetConverter.withDefaults(Collections.emptyMap());

  private final Map<Long, String> rutebankenOrganisations;

  private final boolean administratorAccessActivated;

  private final Map<String, String> authorizedProvidersForNetexBlocksConsumer;

  public EnturPartnerAuth0RolesClaimAdapter(
    @Value(
      "#{${netex.export.block.authorization}}"
    ) Map<String, String> authorizedProvidersForNetexBlocksConsumer,
    @Value(
      "#{${nuska.oauth2.resourceserver.auth0.partner.organisations}}"
    ) Map<Long, String> rutebankenOrganisations,
    @Value(
      "${nuska.oauth2.resourceserver.auth0.partner.admin.activated:false}"
    ) boolean administratorAccessActivated
  ) {
    this.authorizedProvidersForNetexBlocksConsumer =
      authorizedProvidersForNetexBlocksConsumer;
    this.rutebankenOrganisations = rutebankenOrganisations;
    this.administratorAccessActivated = administratorAccessActivated;
  }

  @Override
  public Map<String, Object> convert(Map<String, Object> claims) {
    // delegate to the default RoR claim converter
    Map<String, Object> convertedClaims = this.delegate.convert(claims);

    List<String> audiences = (List<String>) claims.get(OPENID_AUDIENCE_CLAIM);
    if (audiences == null) {
      throw new IllegalStateException(
        "The token must contain an audience claim"
      );
    }

    // otherwise this is an external machine-to-machine token and the custom claim mapping is applied.
    Long enturOrganisationId = (Long) claims.get(ORGANISATION_ID_CLAIM);
    if (enturOrganisationId == null) {
      LOGGER.warn("Organisation ID claim is missing in the token.");
    }
    String rutebankenOrganisationId = getRutebankenOrganisationId(
      enturOrganisationId
    );
    List<String> roleAssignments = new ArrayList<>(2);

    // Add role to edit data from own organization
    String roleRouteData = administratorAccessActivated &&
      isEnturUser(rutebankenOrganisationId)
      ? AuthorizationConstants.ROLE_ROUTE_DATA_ADMIN
      : AuthorizationConstants.ROLE_ROUTE_DATA_EDIT;

    RoleAssignment.Builder routeDataRoleAssignmentBuilder =
      RoleAssignment.builder();
    routeDataRoleAssignmentBuilder.withRole(roleRouteData);
    routeDataRoleAssignmentBuilder.withOrganisation(rutebankenOrganisationId);
    roleAssignments.add(toJSON(routeDataRoleAssignmentBuilder.build()));

    // Add role to view NeTEx Blocks belonging to other organizations
    for (String authorizedNetexBlocksProviderForConsumer : getNetexBlocksProvidersForConsumer(
      rutebankenOrganisationId
    )) {
      RoleAssignment.Builder netexBlockRoleAssignmentBuilder =
        RoleAssignment.builder();
      netexBlockRoleAssignmentBuilder.withRole(
        AuthorizationConstants.ROLE_NETEX_BLOCKS_DATA_VIEW
      );
      netexBlockRoleAssignmentBuilder.withOrganisation(
        authorizedNetexBlocksProviderForConsumer
      );
      roleAssignments.add(toJSON(netexBlockRoleAssignmentBuilder.build()));
    }

    convertedClaims.put(
      RoROAuth2Claims.OAUTH2_CLAIM_ROLE_ASSIGNMENTS,
      roleAssignments
    );

    return convertedClaims;
  }

  private boolean isEnturUser(String rutebankenOrganisationId) {
    return ORG_RUTEBANKEN.equals(rutebankenOrganisationId);
  }

  /**
   * Return the list of codespaces for which the organization can view NeTEx block data.
   */
  private List<String> getNetexBlocksProvidersForConsumer(
    String rutebankenOrganisationId
  ) {
    if (
      authorizedProvidersForNetexBlocksConsumer.get(rutebankenOrganisationId) ==
      null
    ) {
      return Collections.emptyList();
    } else {
      return Arrays.asList(
        authorizedProvidersForNetexBlocksConsumer
          .get(rutebankenOrganisationId)
          .split(",")
      );
    }
  }

  private String getRutebankenOrganisationId(Long enturOrganisationId) {
    return Optional
      .ofNullable(rutebankenOrganisations.get(enturOrganisationId))
      .orElseThrow(() ->
        new IllegalArgumentException(
          "unknown organisation " + enturOrganisationId
        )
      );
  }

  private String toJSON(RoleAssignment roleAssignment) {
    try {
      return ROLE_ASSIGNMENT_OBJECT_WRITER.writeValueAsString(roleAssignment);
    } catch (IOException e) {
      throw new NuskaException(e);
    }
  }
}
