package dk.magenta.datafordeler.core.plugin;

import dk.magenta.datafordeler.plugindemo.DemoPlugin;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by lars on 15-05-17.
 */
public abstract class PluginTestBase {

    @Autowired
    protected DemoPlugin plugin;


    protected String getPayload(String resourceName) throws IOException {
        return IOUtils.toString(PluginTestBase.class.getResourceAsStream(resourceName), StandardCharsets.UTF_8);
    }


    protected String hash(String input) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(input.getBytes());
            byte[] digest = md.digest();
            return DatatypeConverter.printHexBinary(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

}
