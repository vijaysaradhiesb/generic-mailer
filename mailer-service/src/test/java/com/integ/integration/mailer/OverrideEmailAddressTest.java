package com.integ.integration.mailer;


import com.google.common.collect.Sets;
import junit.framework.Assert;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;
import org.junit.Test;
import com.integ.mailer.*;

import java.util.HashSet;
import java.util.Map;

@SuppressWarnings("deprecation")
public class OverrideEmailAddressTest {

	private static final String RECIPIENT = "someone@integ.com";
	private static final String OVERRIDE_EMAIL = "overrideEmail@integ.com";
	private static final String WHITELIST_SUFFIX_EMAIL = "";
	private static final String WHITELIST_SUFFIX_DOMAIN = "";


    /**
     * White lists are empty and email override is NON empty. So all emails should be overridden with overrideEmail@integ.com.
     * 
     * @throws Exception
     */
    @Test
    public void overrideEmailAddressTest() throws Exception {
    	
    	WhitelistChecker whitelist = new WhitelistChecker(WHITELIST_SUFFIX_EMAIL, WHITELIST_SUFFIX_DOMAIN, OVERRIDE_EMAIL);
        XMLMessageMailerProcessor processor = new XMLMessageMailerProcessor();
        processor.setWhiteList(whitelist);

        CamelContext context = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(context);
        Message message = new DefaultMessage();
        message.setMessageId(MESSAGE_ID);
        exchange.setIn(message);

        MailMessage mailMessage = new MailMessage();
        mailMessage.getTos().add(RECIPIENT);
        mailMessage.getCcs().add(RECIPIENT);
        mailMessage.getBccs().add(RECIPIENT);
        mailMessage.setFrom(RECIPIENT);
        mailMessage.setReplyTo(RECIPIENT);
        mailMessage.setBody("Body text");
        mailMessage.setSubject("This is the subject");
        mailMessage.setContentType("text/html");
        exchange.getIn().setBody(mailMessage);
        processor.process(exchange);

        Message out = exchange.getOut();
        Map<String, Object> headers = out.getHeaders();
        Assert.assertEquals(OVERRIDE_EMAIL, headers.get("to"));
    }
    
    /**
     * White lists are NON empty and email override is NON empty. 
     * 
     * So emails not allowed by whitelist should be overridden with overrideEmail@integ.com.
     * 
     * @throws Exception
     */
    @Test
    public void whiteListsNonEmptyOverrideEmailAddressNonEmptyTest() throws Exception {
    	
    	WhitelistChecker whitelist = new WhitelistChecker("allow@integ.com", "gmail.com,hotmail.com", OVERRIDE_EMAIL);
        XMLMessageMailerProcessor processor = new XMLMessageMailerProcessor();
        processor.setWhiteList(whitelist);

        CamelContext context = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(context);
        Message message = new DefaultMessage();
        message.setMessageId(MESSAGE_ID);
        exchange.setIn(message);

        MailMessage mailMessage = new MailMessage();
        mailMessage.getTos().add("allow@integ.com");
        mailMessage.getTos().add("allow@integ.com");
        mailMessage.getTos().add("allow@gmaIl.com");
        mailMessage.getTos().add("allow@HotMail.com");
        mailMessage.getTos().add("replace@yahOO.com");
        
        mailMessage.getCcs().add("allow@integ.com");
        mailMessage.getCcs().add("allow@integ.com");
        mailMessage.getCcs().add("allow@gmaIl.com");
        mailMessage.getCcs().add("allow@HotMail.com");
        mailMessage.getCcs().add("replace@yahOO.com");
        
        mailMessage.getBccs().add("allow@integ.com");
        mailMessage.getBccs().add("allow@integ.com");
        mailMessage.getBccs().add("allow@gmaIl.com");
        mailMessage.getBccs().add("allow@HotMail.com");
        mailMessage.getBccs().add("replace@yahOO.com");
        
        mailMessage.setFrom(RECIPIENT);
        mailMessage.setReplyTo(RECIPIENT);
        mailMessage.setBody("Body text");
        mailMessage.setSubject("This is the subject");
        mailMessage.setContentType("text/html");
        exchange.getIn().setBody(mailMessage);
        processor.process(exchange);

        Message out = exchange.getOut();
        Map<String, Object> headers = out.getHeaders();

        HashSet<String> toEmails = Sets.newHashSet(((String) headers.get("to")).split(WhitelistChecker.SPLIT_CHAR));
        HashSet<String> ccEmails = Sets.newHashSet(((String) headers.get("cc")).split(WhitelistChecker.SPLIT_CHAR));
        HashSet<String> bccEmails = Sets.newHashSet(((String) headers.get("bcc")).split(WhitelistChecker.SPLIT_CHAR));

        HashSet<String> expectedToEmails = Sets.newHashSet("allow@integ.com,overrideEmail@integ.com,allow@HotMail.com,allow@gmaIl.com"
                .split(WhitelistChecker.SPLIT_CHAR));

        org.junit.Assert.assertTrue(toEmails.equals(expectedToEmails));
        org.junit.Assert.assertTrue(ccEmails.equals(expectedToEmails));
        org.junit.Assert.assertTrue(bccEmails.equals(expectedToEmails));
    }
    
    /**
     * White lists are NON empty and email override is empty. 
     * 
     * So emails not allowed should be REMOVED.
     * 
     * @throws Exception
     */
    @Test
    public void whiteListsNonEmptyOverrideEmailAddressEmptyTest() throws Exception {
    	
    	WhitelistChecker whitelist = new WhitelistChecker("allow@integ.com", "gmail.com,hotmail.com", "");
        XMLMessageMailerProcessor processor = new XMLMessageMailerProcessor();
        processor.setWhiteList(whitelist);

        CamelContext context = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(context);
        Message message = new DefaultMessage();
        message.setMessageId(MESSAGE_ID);
        exchange.setIn(message);

        MailMessage mailMessage = new MailMessage();
        mailMessage.getTos().add("allow@integ.com");
        mailMessage.getTos().add("allow@integ.com");
        mailMessage.getTos().add("allow@gmaIl.com");
        mailMessage.getTos().add("allow@HotMail.com");
        mailMessage.getTos().add("replace@yahOO.com");
        
        mailMessage.getCcs().add("allow@integ.com");
        mailMessage.getCcs().add("allow@integ.com");
        mailMessage.getCcs().add("allow@gmaIl.com");
        mailMessage.getCcs().add("allow@HotMail.com");
        mailMessage.getCcs().add("replace@yahOO.com");
        
        mailMessage.getBccs().add("allow@integ.com");
        mailMessage.getBccs().add("allow@integ.com");
        mailMessage.getBccs().add("allow@gmaIl.com");
        mailMessage.getBccs().add("allow@HotMail.com");
        mailMessage.getBccs().add("replace@yahOO.com");
        
        mailMessage.setFrom(RECIPIENT);
        mailMessage.setReplyTo(RECIPIENT);
        mailMessage.setBody("Body text");
        mailMessage.setSubject("This is the subject");
        mailMessage.setContentType("text/html");
        exchange.getIn().setBody(mailMessage);
        processor.process(exchange);

        Message out = exchange.getOut();
        Map<String, Object> headers = out.getHeaders();

        HashSet<String> expectedToEmails = Sets.newHashSet("allow@integ.com,allow@HotMail.com,allow@gmaIl.com"
                .split(WhitelistChecker.SPLIT_CHAR));

        HashSet<String> toEmails = Sets.newHashSet(((String) headers.get("to")).split(WhitelistChecker.SPLIT_CHAR));
        HashSet<String> ccEmails = Sets.newHashSet(((String) headers.get("cc")).split(WhitelistChecker.SPLIT_CHAR));
        HashSet<String> bccEmails = Sets.newHashSet(((String) headers.get("bcc")).split(WhitelistChecker.SPLIT_CHAR));

        org.junit.Assert.assertTrue(toEmails.equals(expectedToEmails));
        org.junit.Assert.assertTrue(ccEmails.equals(expectedToEmails));
        org.junit.Assert.assertTrue(bccEmails.equals(expectedToEmails));


    }

    /**
     * All emails are allowed as both white lists are empty and email override is empty also.
     * 
     * @throws Exception
     */
    @Test
    public void doNotOverrideEmailAddressTest() throws Exception {

    	WhitelistChecker whitelist = new WhitelistChecker(WHITELIST_SUFFIX_EMAIL, WHITELIST_SUFFIX_DOMAIN, "");
        XMLMessageMailerProcessor processor = new XMLMessageMailerProcessor();
        processor.setWhiteList(whitelist);

        CamelContext context = new DefaultCamelContext();
        Exchange exchange = new DefaultExchange(context);
        Message message = new DefaultMessage();
        message.setMessageId(MESSAGE_ID);
        exchange.setIn(message);

        MailMessage mailMessage = new MailMessage();
        mailMessage.getTos().add(RECIPIENT);
        mailMessage.setFrom(RECIPIENT);
        mailMessage.setReplyTo(RECIPIENT);
        mailMessage.setBody("Body text");
        mailMessage.setSubject("This is the subject");
        mailMessage.setContentType("text/html");
        exchange.getIn().setBody(mailMessage);
        processor.process(exchange);

        Message out = exchange.getOut();
        Map<String, Object> headers = out.getHeaders();
        Assert.assertEquals("someone@integ.com", headers.get("to"));
    }

    private static final String MESSAGE_ID = "100";
}
