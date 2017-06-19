package dk.magenta.datafordeler.core.user;

import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import javax.servlet.http.HttpServletRequest;
import org.opensaml.saml2.core.Assertion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by jubk on 13-06-2017.
 */
@Component
public class DafoUserManager {

  @Autowired
  private TokenParser tokenParser;
  @Autowired
  private TokenVerifier tokenVerifier;

  public DafoUserDetails getUserFromRequest(HttpServletRequest request)
      throws InvalidTokenException {

    // If an authorization header starting with "SAML " is provided, use it to create a
    // SAML token based user.
    String authHeader = request.getHeader("Authorization");
    if(authHeader != null && authHeader.indexOf("SAML ") == 0) {
      Assertion samlAssertion = tokenParser.parseAssertion(authHeader.substring(5));
      tokenVerifier.verifyAssertion(samlAssertion);
      return new SamlDafoUserDetails("");
    }
    // Fall back to an anonymous user
    return new AnonymousDafoUserDetails();
  }

}
