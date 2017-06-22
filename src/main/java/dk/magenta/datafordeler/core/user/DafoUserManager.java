package dk.magenta.datafordeler.core.user;

import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import javax.servlet.http.HttpServletRequest;
import org.opensaml.saml2.core.Assertion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Manages DAFO users that are created from incoming SAML tokens.
 * This default implementation is not database backed and will not look up additional details
 * from a users associanted UserProfiles.
 */
public class DafoUserManager {

  @Autowired
  private TokenParser tokenParser;
  @Autowired
  private TokenVerifier tokenVerifier;

  /**
   * Parses and verifies a string containing a deflated and base64 encoded SAML token.
   * @param tokendata - A deflated and base64 encoded SAML token
   * @return A verified Assertion object
   * @throws InvalidTokenException
   */
  public Assertion parseAndVerifyToken(String tokendata) throws InvalidTokenException {
    Assertion samlAssertion = tokenParser.parseAssertion(tokendata);
    tokenVerifier.verifyAssertion(samlAssertion);
    return samlAssertion;
  }


  /**
   * Creates a DafoUserDetails object from an incoming request. If there is a SAML token on the
   * request that token will parsed and verified. If not an AnonymousDafoUserDetails object will
   * be returned.
   * @param request - a HttpServletRequest
   * @return A DafoUserDetails object
   * @throws InvalidTokenException
   */
  public DafoUserDetails getUserFromRequest(HttpServletRequest request)
      throws InvalidTokenException {

    // If an authorization header starting with "SAML " is provided, use it to create a
    // SAML token based user.
    String authHeader = request.getHeader("Authorization");
    if(authHeader != null && authHeader.indexOf("SAML ") == 0) {
      Assertion samlAssertion = parseAndVerifyToken(authHeader.substring(5));
      SamlDafoUserDetails samlDafoUserDetails = new SamlDafoUserDetails(samlAssertion);
      this.addUserProfilesToSamlUser(samlDafoUserDetails);

      return samlDafoUserDetails;
    }
    // Fall back to an anonymous user
    return new AnonymousDafoUserDetails();
  }

  /**
   * Populates a SamlDafoUserDetails object with UserProfiles by translating the UserProfile
   * names gotten from the original SAML token to UserProfile objects.
   * @param samlDafoUserDetails
   */
  public void addUserProfilesToSamlUser(SamlDafoUserDetails samlDafoUserDetails) {
    for(String profileName : samlDafoUserDetails.getAssertionUserProfileNames()) {
      samlDafoUserDetails.addUserProfile(new UserProfile(profileName));
    }
  }
}
