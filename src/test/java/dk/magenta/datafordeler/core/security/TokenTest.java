package dk.magenta.datafordeler.core.security;


import dk.magenta.datafordeler.core.TestConfig;
import dk.magenta.datafordeler.core.user.TokenVerifier;
import java.io.IOException;
import java.util.Collections;
import javax.annotation.PostConstruct;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * Created by jubk on 19-06-2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TokenTest {

  private class TokenHeaderInjector implements ClientHttpRequestInterceptor {

    private String tokenString;

    public TokenHeaderInjector() {
      this.tokenString = "";
    }

    public String getTokenString() {
      return tokenString;
    }

    public void setTokenString(String tokenString) {
      this.tokenString = tokenString;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
        ClientHttpRequestExecution execution) throws IOException {
      if(tokenString != null && !tokenString.equals("")) {
        request.getHeaders().set("Authorization", "SAML " + tokenString);
      }

      return execution.execute(request, body);
    }
  }

  @Autowired
  private TestRestTemplate template;
  @SpyBean
  TokenVerifier tokenVerifier;
  private TokenHeaderInjector tokenHeaderInjector;

  @PostConstruct
  public void postContruct() {
    tokenHeaderInjector = new TokenHeaderInjector();
    template.getRestTemplate().setInterceptors(Collections.singletonList(tokenHeaderInjector));
  }


  @Test
  public void testParseInvalidToken() throws Exception {
    // Make token string invalid
    tokenHeaderInjector.setTokenString("Invalid token string");
    ResponseEntity<String> result = template.getForEntity(
        "/demo/postnummer/1/rest/search?postnr=*",
        String.class
    );
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void testParseValidToken() throws Exception {
    // Skip checks for token age
    doNothing().when(tokenVerifier).verifyTokenAge(anyObject());

    // Make token string invalid
    tokenHeaderInjector.setTokenString(
        "rVfZkqrKEn33Kwz3o2EDToix23MZRFEGAUXxjaEYFEGpYvLrL9qtu7v3dG7EfSOzslauykyysr7/U56iZg5S"
            + "GCbxa4t4wVtNEDuJG8b+a2uz5juj1j+TxndonaLumIYQpKi2bAoQZkCIIbJi9Nrq4gTZwYcdgloT+H"
            + "gwGPcHL/3hcN9qGg/k7g259hXD8R3rtZWl8TixYAjHsXUCcIycsU5L4ri2HFsPR489JXxtBQidxxhW"
            + "FMVL0XtJUh/r4jiB7SRRdwJwslqTd5p3cumEs7yko6/179gn9XcXjvXQjy2UpeAd3v0dPI7hFFbbuD"
            + "D0v7Wee4ErxF5yF1krTuLQsaLwat0YSwAFidukIz9JQxScfsubwG/AHVA6HYfox99a2Gdq/xoI7z8Y"
            + "dk5JCr6l0OrAwOoOhu+QGvBAWmcVNDea8Np6O8Y6tWLoJekJfhb/6vBTSECcgyg5A7cDH7zfnf57wF"
            + "+EYvIdOGMhdqIMhjmQb/VxthwAm6sUeGEphrCuuhI+qgM4/1OUsY/0vohv0eBCH0D0LzPwKSB14In3"
            + "CLyBGFaUgYmDjwyE1ArJnqPsbQwZ56MdHVY50X+9E/hofFc8k/Ymfqm6Z5W87RCpEt9yBdlrazHfxZ"
            + "1wVJBIa0czIa/g0FdQ+3CRWN7lnK6wiYoipv2zJ/HSca5aUm6KM1ydyqfjLmsraYhX+xRjV1mAUdSZ"
            + "XyXXYC+u6WVP5XpteXg2zEJsby4WdfTUNVBHpLFwiZgbzLWlNdJPAczOprhcUGtnUApLzs5O3jA49K"
            + "e4c9xdBoV8uGSnFeiycMii6GgyrnfhLwkVx0Vl4oaxI6rl0JB1VxKdLhcrRKUYinQEI3qRY5QkS8Sy"
            + "WLZxWeKd1aU7y/Rz18ouhh/Ii4Jc4ideM4E/ONsjSsJZ0zZpdpbPuvuLk7fteU/klV77nLNmmS8LJP"
            + "sSTvn9kMu0IaoWO4+2B87FSUQNHomr//r6DP2HWN/CvwTVMxW7AU5xFrKeAntrXV7dEhCYSILAtq8s"
            + "SwPFpwuBoX1hQS8ljxfJOVvye4nGZ6x+memC3ePUKcMWG1pi1HJ6oFXGlw2GliR2W+Zml4cNa0ZdXY"
            + "4GfIGX8pXGpcOmUNbqVV67Vq2r3nTSU/c7oBtOw+VqVrpQcKq5WCZ7Icgdma4ZMCrN+f50Rd/W1YSt"
            + "vxlaUbtWVXriDre302AabUe9eAujJXMQTXNUNvCcaKc5FaAZTJOtE+UZBVOO1AYXmiIpe46Xu54n4P"
            + "pepP3qRJqKaE9n0vaw3c8H214fLTdM0Q2YbGq5iwatFJv+fGZxfAExb51FkXnluFDd9GQ9ioW0CPTT"
            + "dmEe/E2Pko+zDL8kzPFMlmRqhS4eFBfl2MeVgaHpXdggL8Pdri3Y13Uw4KvExRdIT0ehZWfiDHXLrW"
            + "vL5ugQJSRzlMICVH7cwwi4cq8b9WT20ipZx+Lg0N/iy2TREC6XrL+Zaxo53VJat0KVntqICQYDw0kE"
            + "kbWZatXuM+1kRA2ztda+YmDv8yIngG1PtKrzTITH+icllAVGNnDClSL5yNLFlKYtWZWm/WKumpyh4Q"
            + "rDmFNe2R6vK+ug7nkOaBp/lGPNpCocj7Q6s6MZTWxcoVBNibHoxh+N6eJuPC1UXqqri/ZGX7Muvmed"
            + "oZfOqWFCh9kH5mGKtldnAN3tzM2Fkb+ofDPg9f0ia1/ba0orhjroi4y3oFf9MLPMlFNODIY0OO/TUV"
            + "5MB4uerjXaR6x73vc1WRST8/zqt+cRt4qPxW5xOeLttAoULq13k3B61P0hw06TYLrY54e+Ms+saWn6"
            + "1yup6bNeX6TixkAcIo+M8kqa7pYzlAeokiypH83Z1eB6hU5OCIPLYGWRESxVwhiVhmTtr9WUKz1ix+"
            + "+iQ9vq8bS2w45B3hh6Mn0pvHmIXIP0tptEXl95K9Ni39ONHWnMy+h42kcro5eiuVj3Y3nDrLUojJ0V"
            + "0aXiRcLLVbxswyLFjcbQnhqHqAhlW7LJi742M6yXsJQiXDa2z65JNJTeusnXDvFUvvUQ7GN3+dR9Ht"
            + "ONntkH4KCHeLscBa7J13eXhX4/VREvxF0Tuh3vbjrOYngGTk0DuK3mDUXN6iGmFtPX1h3UfSpaE+tU"
            + "f4L/1FNZHjogc+3KBemLHz1mqzcWXyiySeyFN1/1WPSHpdvBm3KCGFAzA3+aJmsjJVZS2kM3kh/tiD"
            + "H+w+52xf+BCPbLONY2bngzgP9XLg94OnPD222u1bd8GjofQ/JYmqwVThlb71KnntUeVJ8WXxWf0LCv"
            + "B/nhAAWxjupqO4EYNe/i32f3T7trUARK9CsdG9XTev13TP440Dtj52ZXqz/U3Y/j/ALuV4uflc8jPW"
            + "mhOhp2hsDvV+6V/jbIwXqSc+vaq8sYq8mFJ4hldYGf08QLI9D6aev9+n++R8K/P0g64T3IDqjnVBiO"
            + "UXUGt6l1fMtZ7LcmHM0rTf3TL/U83yenk5/UP2s+HBn78lqb/Bc=");
    ResponseEntity<String> result = template.getForEntity(
        "/demo/postnummer/1/rest/search?postnr=*",
        String.class
    );

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
}
