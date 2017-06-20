package dk.magenta.datafordeler.core.user;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import org.opensaml.Configuration;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.security.MetadataCredentialResolver;
import org.opensaml.util.resource.ClasspathResource;
import org.opensaml.util.resource.ResourceException;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Created by jubk on 19-06-2017.
 */
@org.springframework.context.annotation.Configuration
@EnableConfigurationProperties(TokenConfigProperties.class)
public class SamlMetadataConfiguration {

  @Bean
  public MetadataProvider samlMetadataProvider(TokenConfigProperties config)
      throws ResourceException, MetadataProviderException, URISyntaxException {
    String path = config.getIssuerMetadataPath();
    if(path == null) {
      // TODO: Log warning about using the default issuer metadata
      ClasspathResource issuerXmlResource = new ClasspathResource("/saml/sts_metadata.xml");
      path = new URI(issuerXmlResource.getLocation()).getPath();
    }
    FilesystemMetadataProvider provider = new FilesystemMetadataProvider(new File(path));
    provider.setRequireValidMetadata(false);
    provider.setParserPool(new BasicParserPool());
    provider.initialize();
    return provider;
  }

  @Bean
  public ExplicitKeySignatureTrustEngine trustEngine(MetadataProvider metadataProvider) {
    MetadataProvider mdProvider = metadataProvider;
    MetadataCredentialResolver mdCredResolver = new MetadataCredentialResolver(mdProvider);
    KeyInfoCredentialResolver keyInfoCredResolver =
        Configuration.getGlobalSecurityConfiguration().getDefaultKeyInfoCredentialResolver();
    return new ExplicitKeySignatureTrustEngine(mdCredResolver, keyInfoCredResolver);
  }


}
