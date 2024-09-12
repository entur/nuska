package no.entur.nuska.config.oauth2;

import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.IOException;
import java.util.*;
import no.entur.nuska.NuskaException;
import org.entur.oauth2.RoROAuth2Claims;
import org.entur.oauth2.RorAuth0RolesClaimAdapter;
import org.rutebanken.helper.organisation.AuthorizationConstants;
import org.rutebanken.helper.organisation.RoleAssignment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;

/**
 * Insert a "role_assignments" claim in the JWT token based on the organisationID claim, for compatibility with the existing
 * authorization process (@{@link org.entur.oauth2.JwtRoleAssignmentExtractor}).
 * This custom mapping is intended for tokens sent from external client that do not contain the role_assignments nor the permissions claim.
 */
public class EnturPartnerAuth0RolesClaimAdapter
  implements Converter<Map<String, Object>, Map<String, Object>> {

  static final String ORG_RUTEBANKEN = "RB";
  static final String OPENID_AUDIENCE_CLAIM = "aud";
  static final String ORGANISATION_ID_CLAIM = "https://entur.io/organisationID";

  private static final ObjectWriter ROLE_ASSIGNMENT_OBJECT_WRITER =
    ObjectMapperFactory.getSharedObjectMapper().writerFor(RoleAssignment.class);

  private final RorAuth0RolesClaimAdapter delegate;

  private final Map<Long, String> rutebankenOrganisations;

  private final boolean administratorAccessActivated;

  private final Map<String, String> authorizedProvidersForNetexBlocksConsumer;

  private final String rorAuth0Audience;

  public EnturPartnerAuth0RolesClaimAdapter(
    @Value(
      "#{${netex.export.block.authorization}}"
    ) Map<String, String> authorizedProvidersForNetexBlocksConsumer,
    @Value(
      "#{${nuska.oauth2.resourceserver.auth0.partner.organisations}}"
    ) Map<Long, String> rutebankenOrganisations,
    @Value(
      "${nuska.oauth2.resourceserver.auth0.partner.admin.activated:false}"
    ) boolean administratorAccessActivated,
    @Value(
      "${nuska.oauth2.resourceserver.auth0.ror.claim.namespace}"
    ) String rorAuth0ClaimNamespace,
    @Value(
      "${nuska.oauth2.resourceserver.auth0.ror.jwt.audience}"
    ) String rorAuth0Audience
  ) {
    this.authorizedProvidersForNetexBlocksConsumer =
      authorizedProvidersForNetexBlocksConsumer;
    this.rutebankenOrganisations = rutebankenOrganisations;
    this.administratorAccessActivated = administratorAccessActivated;
    this.rorAuth0Audience = rorAuth0Audience;
    this.delegate = new RorAuth0RolesClaimAdapter(rorAuth0ClaimNamespace);
  }

  @Override
  public Map<String, Object> convert(Map<String, Object> claims) {
    // delegate to the default RoR claim converter
    Map<String, Object> convertedClaims = this.delegate.convert(claims);

    List<String> audiences = (List<String>) convertedClaims.get(
      OPENID_AUDIENCE_CLAIM
    );
    if (audiences == null) {
      throw new IllegalStateException(
        "The token must contain an audience claim"
      );
    }

    // if the token contains the Ror audience, fall back to the default claim mapping
    if (audiences.contains(rorAuth0Audience)) {
      return convertedClaims;
    }

    // otherwise this is an external machine-to-machine token and the custom claim mapping is applied.
    Long enturOrganisationId = (Long) convertedClaims.get(
      ORGANISATION_ID_CLAIM
    );
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

    // Add a preferred name to be displayed in Ninkasi
    convertedClaims.put(
      StandardClaimNames.PREFERRED_USERNAME,
      rutebankenOrganisationId + " (File transfer via API)"
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
