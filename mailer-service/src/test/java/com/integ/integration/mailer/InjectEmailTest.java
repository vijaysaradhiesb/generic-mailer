package com.integ.integration.mailer;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Test;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Use this to check that emails can be sent
 */
public class InjectEmailTest {

    private static final String RECIPIENT = "someone@integ.com";
    private static final String URL = "mock:out";


    @Test
    public void injectMessage() throws IOException {
        CamelContext context = new DefaultCamelContext();
        ProducerTemplate producerTemplate = context.createProducerTemplate();
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("to", RECIPIENT);
        headers.put("from", RECIPIENT);
        headers.put("replyTo", RECIPIENT);
        headers.put("subject", "this is the subject");
        headers.put("contentType", "text/html");
        producerTemplate.sendBodyAndHeaders(URL, "this is the body", headers);
    }
}
