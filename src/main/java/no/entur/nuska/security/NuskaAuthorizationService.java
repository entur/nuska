package no.entur.nuska.security;

/**
 *  Service that verifies the privileges of the API clients.
 */
public interface NuskaAuthorizationService {
  /**
   * Verify that the user can read block data for a given provider.
   * Users can download NeTEx blocks data if they have administrator privileges,
   * or if they have editor privileges for this provider
   * or if they have NeTEx blocks viewer privileges for this provider.
   */
  void verifyBlockViewerPrivileges(String providerId);
}
