package com.integ.integration.mailer;

import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.converter.IOConverter;
import org.apache.camel.model.ModelCamelContext;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import com.integ.mailer.*;


public abstract class XaMailerTestSupport {
    protected static final String START = "direct:start";
    protected static final String RESULT = "mock:result";

    @EndpointInject(uri = RESULT)
    protected MockEndpoint result;

    @EndpointInject(uri = START)
    protected ProducerTemplate start;

    @Autowired
    protected ModelCamelContext context;

    protected String email;

    @Before
    public void setup() throws Exception {
        Resource inputFile = new ClassPathResource("huge_email_exchange.xml");
        email = IOConverter.toString(inputFile.getFile(), null);

        if (context == null) {
            return;
        }

        context.setTracing(true);

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from(START).transacted("PROPAGATION_REQUIRES_NEW").to("jmstx:queue:xmlMessage.start");
            }
        });

        context.getRouteDefinition("xmlMessageMailer").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveById("smtpXmlMessageDestination")
                        .replace()
                        .to(RESULT);
            }
        });

        context.start();
    }

    public String toXml(MailMessage mailMessage) throws JAXBException {
        Marshaller m = JAXBContext.newInstance(MailMessage.class).createMarshaller();
        StringWriter stringWriter = new StringWriter();
        m.marshal(mailMessage, stringWriter);
        return stringWriter.toString();
    }
}
