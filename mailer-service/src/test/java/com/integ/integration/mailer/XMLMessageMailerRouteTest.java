package com.integ.integration.mailer;

import org.apache.camel.test.spring.UseAdviceWith;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.integ.mailer.*;

import javax.activation.DataHandler;
import java.io.IOException;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/test-connectivity/broker1.xml",
        "classpath:/test-connectivity/connection-jms1.xml",
        "classpath:test-context.xml",
        "classpath:META-INF/spring/tm-config.xml",
        "classpath:META-INF/spring/jms.xml",
        "classpath:META-INF/spring/camel-context.xml",
        "classpath:test-whitelist-definition-context-1.xml"
})
@UseAdviceWith
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class XMLMessageMailerRouteTest extends XaMailerTestSupport {
    protected static final String EXPECTED_BODY = "Your broker statements for 01/01/2015 are attached to this email";
    protected static final String EXPECTED_ATTACH_CONTENT = "This is the attachment body";
    protected static final String EXPECTED_ATTACH_NAME = "testAttachment.txt";
    
    @Value("#{props['email.subject.prefix']}")
    private String subjectPrefix;

    public XMLMessageMailerRouteTest() throws Exception {
        super();
    }

    @Test
    public void testRegular() throws Exception {

        MailMessage message = new MailMessage();
        message.getTos().add("test@yahoo.com");
        message.getBccs().add("test@yahoo.com");
        message.getBccs().add("test2@yahoo.com");
        message.getCcs().add("test@yahoo.com");
        message.setFrom("hello@yahoo.com");
        message.setBody(EXPECTED_BODY);
        message.setSubject("I am the subject");

        Attachment attachment = new Attachment();
        attachment.setAttachmentName(EXPECTED_ATTACH_NAME);
        attachment.setMimeType("text/plain");
        attachment.setContent(EXPECTED_ATTACH_CONTENT.getBytes());
        message.getAttachments().add(attachment);

        String xml = toXml(message);

        result.expectedBodiesReceived(EXPECTED_BODY);

        start.sendBody(xml);

        result.assertIsSatisfied();

        Map<String, DataHandler> attachmentMap = result.getReceivedExchanges().get(0).getIn().getAttachments();

        String toHeader = result.getReceivedExchanges().get(0).getIn().getHeader(XMLMessageMailerProcessor.TO, String.class);
        Assert.assertTrue(toHeader.contains("yahoo.com"));

        String bccHeader = result.getReceivedExchanges().get(0).getIn().getHeader(XMLMessageMailerProcessor.BCC, String.class);
        Assert.assertTrue(bccHeader.contains("yahoo.com"));

        String ccHeader = result.getReceivedExchanges().get(0).getIn().getHeader(XMLMessageMailerProcessor.CC, String.class);
        Assert.assertTrue(ccHeader.contains("yahoo.com"));
        
        String subjectHeader = result.getReceivedExchanges().get(0).getIn().getHeader(XMLMessageMailerProcessor.SUBJECT, String.class);
        Assert.assertTrue(subjectHeader.contains(subjectPrefix));

        Assert.assertEquals("number of attachments", 1, attachmentMap.size());
        Assert.assertNotNull("attachment name present", attachmentMap.get(EXPECTED_ATTACH_NAME));
        Assert.assertEquals("attachment content", EXPECTED_ATTACH_CONTENT, attachmentMap.get(EXPECTED_ATTACH_NAME).getContent());

    }

    /**
     * This method tests that a very big messages can be indeed un-marshalled.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testVeryLargeAttachments() throws IOException, InterruptedException {

        result.expectedBodiesReceived(EXPECTED_BODY);
        result.expectedMessageCount(1);

        start.sendBody(email);

        result.assertIsSatisfied();

    }

}
