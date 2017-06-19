package dk.magenta.datafordeler.core.user;

import dk.magenta.datafordeler.core.exception.InvalidTokenException;
import java.io.File;
import java.util.Timer;
import javax.persistence.criteria.CriteriaBuilder.In;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.saml2.metadata.provider.ResourceBackedMetadataProvider;
import org.opensaml.security.MetadataCredentialResolver;
import org.opensaml.security.MetadataCriteria;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.util.resource.ClasspathResource;
import org.opensaml.util.resource.ResourceException;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.criteria.UsageCriteria;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.trust.TrustEngine;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.opensaml.xml.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Verifies a DAFO token according to expiration, issuer, signature and audience restriction.
 */
@Component
@EnableConfigurationProperties(TokenConfigProperties.class)
public class TokenVerifier {

  @Autowired
  private MetadataProvider metadataProvider;
  @Autowired
  private TrustEngine trustEngine;
  @Autowired
  private TokenConfigProperties config;

  public TokenVerifier() throws ConfigurationException {
    // Initialize OpenSAML
    org.opensaml.DefaultBootstrap.bootstrap();
  }

  public EntityDescriptor getIssuerEntityDescriptor() throws InvalidTokenException {
    try {
      return (EntityDescriptor) metadataProvider.getMetadata();
    } catch (MetadataProviderException e) {
      throw new InvalidTokenException("Unable to fetch issuer metadata: " + e.getMessage(), e);
    }
  }


  public static boolean isDateTimeSkewValid(int skewInSec, long forwardInterval, DateTime time) {
    long reference = System.currentTimeMillis();
    return time.isBefore(reference + (skewInSec * 1000)) &&
        time.isAfter(reference - ((skewInSec + forwardInterval) * 1000));
  }

  public void verifyIssuer(Issuer issuer) throws InvalidTokenException {
    // Validate format of issuer
    if (issuer.getFormat() != null && !issuer.getFormat().equals(NameIDType.ENTITY)) {
      throw new InvalidTokenException("Wrong issuer type: " + issuer.getFormat());
    }
    // Validate that issuer is expected peer entity
    String entityid1 = getIssuerEntityDescriptor().getEntityID();
    String entityId2 = issuer.getValue();
    if (!issuer.getValue().equals(getIssuerEntityDescriptor().getEntityID())) {
      throw new InvalidTokenException("Invalid issuer: " + issuer.getValue());
    }
  }

  public void verifySignature(Signature signature) throws InvalidTokenException {
    // Validate the actual signing is correct
    SAMLSignatureProfileValidator validator = new SAMLSignatureProfileValidator();

    try {
      validator.validate(signature);
    } catch (ValidationException e) {
      throw new InvalidTokenException("Invalid token signature: " + e.getMessage(), e);
    }

    // Check list of neccessary criteria that ensures that it is the signature we want and
    // not just any syntactically correct signature.
    CriteriaSet criteriaSet = new CriteriaSet();
    criteriaSet.add(new EntityIDCriteria(getIssuerEntityDescriptor().getEntityID()));
    criteriaSet.add(new MetadataCriteria(
        IDPSSODescriptor.DEFAULT_ELEMENT_NAME, SAMLConstants.SAML20P_NS
    ));
    criteriaSet.add(new UsageCriteria(UsageType.SIGNING));

    boolean criteriaAreValid;
    try {
      criteriaAreValid = trustEngine.validate(signature, criteriaSet);
    } catch (SecurityException e) {
      throw new InvalidTokenException(
          "Security exception while validating token signature: " + e.getMessage(), e
      );
    }

    if (!criteriaAreValid) {
      throw new InvalidTokenException("Signature is not trusted or invalid");
    }

  }

  public void verifyTokenAge(DateTime issueInstant) {
    if(!isDateTimeSkewValid(
        config.getTimeSkewInSeconds(),
        config.getMaxAssertionTimeInSeconds(),
        issueInstant
    )) {
      throw new InvalidTokenException(
          "Token is older than " + config.getMaxAssertionTimeInSeconds() + " seconds"
      );
    }
  }


  public void verifyAssertion(Assertion assertion) throws InvalidTokenException {
    verifyTokenAge(assertion.getIssueInstant());
    verifyIssuer(assertion.getIssuer());
    verifySignature(assertion.getSignature());


    // TODO: Validate expiration
    // TODO: Validate signature
    // TODO: Validate subject
  }

}
