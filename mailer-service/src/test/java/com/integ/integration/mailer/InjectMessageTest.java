package com.integ.integration.mailer;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import com.integ.mailer.*;

/**
 * Use this to test the deployed routes
 */
public class InjectMessageTest extends XaMailerTestSupport {

    private static final String RECIPIENT = "someone@integ.com";
    private static final String BROKER = "mock:out";

    public InjectMessageTest() throws Exception {
        super();
    }


    @Test
    public void injectXMLMessage() throws Exception {

        MailMessage message = new MailMessage();
        message.getTos().add(RECIPIENT);
        message.setFrom(RECIPIENT);
        message.setReplyTo(RECIPIENT);
        message.setBody("this is the xml message body");
        message.setSubject("xml message subject");
        message.setContentType("text/plain");

        Attachment attachment = new Attachment();
        attachment.setAttachmentName("new-name");
        attachment.setMimeType("text/plain");
        attachment.setContent("this is the message attachment content".getBytes());
        message.getAttachments().add(attachment);

        String xml = toXml(message);
        getProducerTemplate().sendBody(BROKER, xml);
    }

    @Test
    public void injectHeaderMessage() throws Exception {

        Map<String,Object> headers = new HashMap<String,Object>();
        headers.put("to", RECIPIENT);
        headers.put("from", RECIPIENT);
        headers.put("replyto", RECIPIENT);
        headers.put("subject", "header message subject");
        headers.put("contentType", "text/plain");

        getProducerTemplate().sendBodyAndHeaders(BROKER, "this is the header message body", headers);
    }

    private ProducerTemplate getProducerTemplate() {
        CamelContext context = new DefaultCamelContext();
        ProducerTemplate producerTemplate = context.createProducerTemplate();
        Assert.assertNotNull("null producerTemplate", producerTemplate);

        return producerTemplate;
    }

}
