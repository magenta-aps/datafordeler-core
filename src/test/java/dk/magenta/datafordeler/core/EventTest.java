package dk.magenta.datafordeler.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.magenta.datafordeler.core.event.BusinessEvent;
import dk.magenta.datafordeler.core.event.EventMessage;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lars on 13-01-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class EventTest {

    @Test
    public void parseJsonEvent() {
        ObjectMapper mapper = new ObjectMapper();
        InputStream input = EventTest.class.getResourceAsStream("/request.json");
        Assert.assertNotNull(input);
        try {
            BusinessEvent event = mapper.readValue(input, BusinessEvent.class);
            Assert.assertEquals("#DataDistributor.Event", event.eventType);
            Assert.assertEquals("JSON", event.format);
            Assert.assertEquals(1, event.id);
            EventMessage eventMessage = event.getEventBody().eventMessage;
            Assert.assertEquals("1.0", eventMessage.messageVersion);
            Assert.assertNull(event.getEventBody().eventMessage.messageId);
            Assert.assertEquals("MatrikulaerSagCreate", eventMessage.messageEnvelope.filterData.messageType);
            Assert.assertNull(eventMessage.messageEnvelope.filterData.messageResponsible);
            Assert.assertNull(eventMessage.messageEnvelope.filterData.allowedRecipient);
            Assert.assertNull(eventMessage.messageEnvelope.filterData.relatedObject);
            Assert.assertEquals(1, eventMessage.messageEnvelope.filterData.objectRegistrations.size());
            Assert.assertEquals("Peter West-Nielsen", eventMessage.messageEnvelope.filterData.objectRegistrations.get(0).registrator);
            Assert.assertNull(eventMessage.messageEnvelope.filterData.objectRegistrations.get(0).registrationTime);
            Assert.assertEquals("Afsluttet", eventMessage.messageEnvelope.filterData.objectRegistrations.get(0).status);
            Assert.assertEquals("Geodatastyrelsen", eventMessage.messageEnvelope.filterData.objectRegistrations.get(0).objectResponsible);
            Assert.assertEquals("ID20165", eventMessage.messageEnvelope.filterData.objectRegistrations.get(0).objectId);
            Assert.assertEquals("MatrikulaerSag", eventMessage.messageEnvelope.filterData.objectRegistrations.get(0).objectType);
            Assert.assertNull(eventMessage.messageEnvelope.filterData.objectRegistrations.get(0).objectAction);
            Assert.assertEquals("52.20.05", eventMessage.messageEnvelope.filterData.objectRegistrations.get(0).objectSubject);
            Assert.assertEquals("5,0001-01-01T00:00:00.0000000", eventMessage.messageEnvelope.filterData.objectRegistrations.get(0).registrationId);
            Assert.assertNull(eventMessage.messageEnvelope.filterData.objectRegistrations.get(0).localization);
            Assert.assertEquals("Diverse", eventMessage.messageEnvelope.filterData.transversalProcess);
            Assert.assertNull(eventMessage.messageEnvelope.deliveryInformation.creationTime);
            Assert.assertNull(eventMessage.messageEnvelope.deliveryInformation.transactionId);
            Assert.assertNull(eventMessage.messageEnvelope.deliveryInformation.sourceSystem);
            Assert.assertNull(eventMessage.messageEnvelope.deliveryInformation.sourceSystemIPAddress);
            Assert.assertNull(eventMessage.messageEnvelope.deliveryInformation.sourceSystemCredentials);
            Assert.assertEquals("Ikke fortrolige data", eventMessage.messageEnvelope.deliveryInformation.securityClassification);
            Assert.assertNull(eventMessage.messageEnvelope.deliveryInformation.deliveryRoute);
            Assert.assertNull(eventMessage.messageEnvelope.recipientAction);
            Assert.assertEquals(1, eventMessage.messageDataList.size());
            Assert.assertEquals("ID20165", eventMessage.messageDataList.get(0).objectReference.objectReference);
            Assert.assertNull(eventMessage.messageDataList.get(0).objectData);
            System.out.println("Test parseJsonEvent complete");
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
}
