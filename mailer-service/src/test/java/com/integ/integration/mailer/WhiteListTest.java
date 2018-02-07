package com.integ.integration.mailer;

import org.apache.camel.test.spring.UseAdviceWith;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.integ.mailer.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/test-connectivity/broker1.xml",
        "classpath:/test-connectivity/connection-jms1.xml",
        "classpath:test-context.xml",
        "classpath:META-INF/spring/tm-config.xml",
        "classpath:META-INF/spring/jms.xml",
        "classpath:META-INF/spring/camel-context.xml",
        "classpath:test-whitelist-definition-context-2.xml"
})
@UseAdviceWith
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class WhiteListTest extends XaMailerTestSupport {
    private static final String EXPECTED_BODY = "Your broker statements for 01/01/2015 are attached to this email";
    private static final String EXPECTED_BODY_OK1 = "ok1";
    private static final String EXPECTED_BODY_OK2 = "ok2";

    public WhiteListTest() throws Exception {
        super();
    }


    /**
     * All non white listed e-mails should be REPLACED from to, bcc and cc headers, with email override override@integ.com.
     * 
     * <p/>
     * If no email remains in To, then the exchange should be filtered out and no exchange should reach the SMTP producer
     * <p/>
     * test-whitelist-definition-context-2.xml defines:
     * 
     *  <constructor-arg value="test1@integ.com"/>
     *  <constructor-arg value="gmail.com,hotmail.com"/>
     *  <constructor-arg value="override@integ.com"/>
     *
     * @throws Exception
     */
    @Test
    public void testWhiteListFiltering() throws Exception {

        // all email addresses are NON valid - should be replaced by override@integ.com
        MailMessage message = new MailMessage();
        message.getTos().add("test@yahoo.com");
        message.getBccs().add("test@yahoo.com");
        message.getBccs().add("test2@yahoo.com");
        message.getCcs().add("test@yahoo.com");
        message.setFrom("hello@yahoo.com");
        message.setBody(EXPECTED_BODY);

        String xml = toXml(message);

        // all email addresses are valid 
        MailMessage message2 = new MailMessage();
        message2.getTos().add("test@gmail.com");
        message2.getBccs().add("test@gmail.com");
        message2.getBccs().add("test2@gmail.com");
        message2.getCcs().add("test@gmail.com");
        message2.setFrom("hello@gmail.com");
        message2.setBody(EXPECTED_BODY);

        String xml2 = toXml(message2);

        // To address in NOT valid
        MailMessage message3 = new MailMessage();
        message3.getTos().add("test@yahoo.com");
        message3.getBccs().add("test@gmail.com");
        message3.getBccs().add("test2@gmail.com");
        message3.getCcs().add("test@gmail.com");
        message3.setFrom("hello@gmail.com");
        message3.setBody(EXPECTED_BODY);

        String xml3 = toXml(message3);

        // Bcc address in NOT valid 
        MailMessage message4 = new MailMessage();
        message4.getTos().add("test@gmail.com");
        // the next address should be removed by white filter
        message4.getTos().add("test@yahoo.com");
        // this one should stay
        message4.getTos().add("test1@integ.com");
        
        message4.getBccs().add("test@yahoo.com");
        message4.getBccs().add("test2@gmail.com");
        message4.getCcs().add("test@gmail.com");
        message4.setFrom("hello@gmail.com");
        message4.setBody(EXPECTED_BODY);

        String xml4 = toXml(message4);

        // Cc address in NOT valid - ok
        MailMessage message5 = new MailMessage();
        message5.getTos().add("test@gmail.com");
        message5.getBccs().add("test@gmail.com");
        message5.getBccs().add("test2@gmail.com");
        message5.getCcs().add("test@yahoo.com");
        // this should stay
        message5.getCcs().add("test1@integ.com");
        message5.setFrom("hello@gmail.com");
        message5.setBody(EXPECTED_BODY);

        String xml5 = toXml(message5);

        result.expectedBodiesReceivedInAnyOrder(EXPECTED_BODY, EXPECTED_BODY, EXPECTED_BODY, EXPECTED_BODY, EXPECTED_BODY);

        start.sendBody(xml);
        start.sendBody(xml2);
        start.sendBody(xml3);
        start.sendBody(xml4);
        start.sendBody(xml5);

        Thread.sleep(3000);

        result.assertIsSatisfied();

        // test that the not white listed email addresses do not exist
        for (int i = 0; i < 5; i++) {

            String toHeader = result.getReceivedExchanges().get(i).getIn().getHeader(XMLMessageMailerProcessor.TO, String.class);
            if (toHeader != null) {
                Assert.assertTrue(!toHeader.contains("yahoo.com"));
            }

            String bccHeader = result.getReceivedExchanges().get(i).getIn().getHeader(XMLMessageMailerProcessor.BCC, String.class);
            if (bccHeader != null) {
                Assert.assertTrue(!bccHeader.contains("yahoo.com"));
            }

            String ccHeader = result.getReceivedExchanges().get(i).getIn().getHeader(XMLMessageMailerProcessor.CC, String.class);
            if (ccHeader != null) {
                Assert.assertTrue(!ccHeader.contains("yahoo.com"));
            }

        }

    }
    
    
    /**
     * ONLY gmail.com,hotmail.com are acceptable.
     * 
     * @throws Exception
     */
    @Test
    public void testCaseInsensitive() throws Exception {

        // all email addresses are valid 
        MailMessage message = new MailMessage();
        message.getTos().add("test@gMail.com");
        message.getTos().add("test@yahoo.com");
        
        message.getBccs().add("test@gmAil.com");
        message.getBccs().add("test2@gmaIl.com");
        
        message.getCcs().add("test@gMaiL.com");
        message.setFrom("hello@GMAIL.com");
        message.setBody(EXPECTED_BODY_OK1);

        String xml = toXml(message);

        // Cc address in NOT valid
        MailMessage message2 = new MailMessage();
        message2.getTos().add("test@gMail.com");
        message2.getTos().add("test@yahoo.com");
        
        message2.getBccs().add("test@gmAil.com");
        message2.getBccs().add("test2@gmaIl.com");
        
        message2.getCcs().add("test@gMaiL.com");
        message2.setFrom("hello@gmail.cOm");
        message2.setBody(EXPECTED_BODY_OK2);

        String xml2 = toXml(message2);

        result.expectedBodiesReceivedInAnyOrder(EXPECTED_BODY_OK1, EXPECTED_BODY_OK2);

        start.sendBody(xml);
        start.sendBody(xml2);

        Thread.sleep(3000);

        result.assertIsSatisfied();

        // test that the not white listed email addresses do not exist
        for (int i = 0; i < 2; i++) {

            String toHeader = result.getReceivedExchanges().get(i).getIn().getHeader(XMLMessageMailerProcessor.TO, String.class);
            if (toHeader != null) {
                Assert.assertTrue(!toHeader.contains("yahoo.com"));
                Assert.assertTrue(toHeader.contains("gMail.com"));
                Assert.assertTrue(toHeader.contains("override@integ.com"));
            }

            String bccHeader = result.getReceivedExchanges().get(i).getIn().getHeader(XMLMessageMailerProcessor.BCC, String.class);
            if (bccHeader != null) {
                Assert.assertTrue(!bccHeader.contains("yahoo.com"));
                Assert.assertTrue(bccHeader.contains("gmAil.com") || bccHeader.contains("gmaIl.com"));
            }

            String ccHeader = result.getReceivedExchanges().get(i).getIn().getHeader(XMLMessageMailerProcessor.CC, String.class);
            if (ccHeader != null) {
                Assert.assertTrue(!ccHeader.contains("yahoo.com"));
                Assert.assertTrue(ccHeader.contains("gMaiL.com"));
            }

        }

    }
}
