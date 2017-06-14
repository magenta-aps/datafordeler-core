package dk.magenta.datafordeler.core.user;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Created by jubk on 13-06-2017.
 */
@Component
public class DafoUserManager {
  public DafoUserDetails getUserFromRequest(HttpServletRequest request) {

    // If an authorization header starting with "SAML " is provided, use it to create a
    // SAML token based user.
    String authHeader = request.getHeader("Authorization");
    if(authHeader != null && authHeader.indexOf("SAML ") == 0) {
      return new SamlDafoUserDetails(authHeader.substring(5));
    }
    // Fall back to an anonymous user
    return new AnonymousDafoUserDetails();
  }
}
