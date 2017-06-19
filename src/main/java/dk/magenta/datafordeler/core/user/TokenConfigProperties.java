package dk.magenta.datafordeler.core.user;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by jubk on 19-06-2017.
 */
@ConfigurationProperties("dafo.tokenvalidation")
public class TokenConfigProperties {
  private int timeSkewInSeconds = 5;

  private long maxAssertionTimeInSeconds = 60 * 60;

  private static String issuerMetadataPath = null;

  public int getTimeSkewInSeconds() {
    return timeSkewInSeconds;
  }

  public void setTimeSkewInSeconds(int timeSkewInSeconds) {
    this.timeSkewInSeconds = timeSkewInSeconds;
  }

  public long getMaxAssertionTimeInSeconds() {
    return maxAssertionTimeInSeconds;
  }

  public void setMaxAssertionTimeInSeconds(long maxAssertionTimeInSeconds) {
    this.maxAssertionTimeInSeconds = maxAssertionTimeInSeconds;
  }

  public static String getIssuerMetadataPath() {
    return issuerMetadataPath;
  }

  public static void setIssuerMetadataPath(String issuerMetadataPath) {
    TokenConfigProperties.issuerMetadataPath = issuerMetadataPath;
  }
}
