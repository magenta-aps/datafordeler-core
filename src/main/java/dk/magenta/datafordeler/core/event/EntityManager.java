package dk.magenta.datafordeler.core.event;

import dk.magenta.datafordeler.core.exception.FailedReferenceException;
import dk.magenta.datafordeler.core.exception.HttpStatusException;
import dk.magenta.datafordeler.core.exception.WrongSubclassException;
import dk.magenta.datafordeler.core.model.Entity;
import dk.magenta.datafordeler.core.model.RegistrationReference;
import dk.magenta.datafordeler.core.model.Registration;
import dk.magenta.datafordeler.core.plugin.Fetcher;
import dk.magenta.datafordeler.core.plugin.RegisterManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

/**
 * Created by lars on 13-03-17.
 *
 * Entity (and associates) specific manager. Subclass in plugins
 * A plugin can have any number of Entity classes, each needing their own way of handling
 * So we split the needed methods on RegisterManager out into these EntityManagers
 */
public abstract class EntityManager {
    protected Class<? extends Entity> managedEntityClass;
    protected Class<? extends RegistrationReference> managedRegistrationReferenceClass;
    protected Class<? extends Registration> managedRegistrationClass;
    protected Fetcher registrationFetcher;

    public abstract RegisterManager getRegisterManager();

    public Class<? extends Entity> getManagedEntityClass() {
        return this.managedEntityClass;
    }

    public Class<? extends RegistrationReference> getManagedRegistrationReferenceClass() {
        return this.managedRegistrationReferenceClass;
    }

    public Class<? extends Registration> getManagedRegistrationClass() {
        return this.managedRegistrationClass;
    }

    public abstract Collection<String> getHandledURISubstrings();

    /**
     * Parse incoming data into a Reference (data coming from within a request envelope)
     * @param referenceData
     * @return
     * @throws IOException
     */
    public abstract RegistrationReference parseReference(InputStream referenceData) throws IOException;

    /**
     * Parse incoming data into a Reference (data coming from within a request envelope)
     * @param referenceData
     * @return
     * @throws IOException
     */
    public abstract RegistrationReference parseReference(String referenceData, String charsetName) throws IOException;

    /**
     * Parse incoming data into a Reference (data coming from within a request envelope)
     * @param uri
     * @return
     */
    public abstract RegistrationReference parseReference(URI uri);

    /**
     * Parse incoming data into a Registration (data coming from within a request envelope)
     * @param registrationData
     * @return
     * @throws IOException
     */
    public abstract Registration parseRegistration(InputStream registrationData) throws IOException;

    /**
     * Parse incoming data into a Registration (data coming from within a request envelope)
     * @param registrationData
     * @return
     * @throws IOException
     */
    public abstract Registration parseRegistration(String registrationData, String charsetName) throws IOException;

    /**
     *
     * @param reference
     * @return
     * @throws WrongSubclassException
     */
    public abstract URI getRegistrationInterface(RegistrationReference reference) throws WrongSubclassException;

    /**
     * Obtain a Registration by Reference
     * @param reference
     * @return Registration object, fetched and parsed by this class implementation
     * @throws WrongSubclassException
     * @throws IOException
     * @throws FailedReferenceException
     */
    public Registration fetchRegistration(RegistrationReference reference) throws WrongSubclassException, IOException, FailedReferenceException {
        if (!this.managedRegistrationReferenceClass.isInstance(reference)) {
            throw new WrongSubclassException(this.managedRegistrationReferenceClass, reference);
        }
        InputStream registrationData = null;
        URI uri = this.getRegistrationInterface(reference);
        try {
            registrationData = this.registrationFetcher.fetch(uri);
        } catch (HttpStatusException e) {
            throw new FailedReferenceException(reference, e);
        }
        return this.parseRegistration(
            registrationData
        );
    }

    /**
     * Utility method to be used by subclasses
     * @param base
     * @param path
     * @return Expanded URI, with scheme, host and port from the base, a custom path, and no query or fragment
     * @throws URISyntaxException
     */
    protected static URI expandBaseURI(URI base, String path) throws URISyntaxException {
        return RegisterManager.expandBaseURI(base, path);
    }
    /**
     * Utility method to be used by subclasses
     * @param base
     * @param path
     * @return Expanded URI, with scheme, host and port from the base, and a custom path query and fragment
     * @throws URISyntaxException
     */
    protected static URI expandBaseURI(URI base, String path, String query, String fragment) throws URISyntaxException {
        return RegisterManager.expandBaseURI(base, path, query, fragment);
    }
}
