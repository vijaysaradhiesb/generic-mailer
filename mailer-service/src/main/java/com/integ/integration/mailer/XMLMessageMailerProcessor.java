package com.integ.integration.mailer;


import com.google.common.base.Joiner;
import com.integ.mailer.Attachment;
import com.integ.mailer.MailMessage;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.mail.internet.InternetAddress;
import javax.mail.util.ByteArrayDataSource;
import java.util.List;

public class XMLMessageMailerProcessor implements Processor {

    String subjectPrefix = "";

    public void process(Exchange exchange) throws Exception {

        MailMessage message = exchange.getIn().getBody(MailMessage.class);
        LOG.debug(String.format("Mailer Received Message  - %s", message));

        Message out = exchange.getOut();

        if (message.getAttachments() != null) {
            for (Attachment a : message.getAttachments()) {
                if (a.getContent() == null) { //This protect us against javax.mail problem with sending null attachment, but we still want to give a sign there was an attempt to send an attachement.
                    a.setContent("NULL attachment was sent to xmlMailer, so mailer set this message to avoid facing exception from javax.mail".getBytes());
                    a.setMimeType("text/plain");
                    a.setAttachmentName("MsgGeneratedByXmlMailer.txt");
                }
                out.addAttachment(a.getAttachmentName(), new DataHandler(new ByteArrayDataSource(a.getContent(), a.getMimeType())));
            }
        }
        out.setBody(message.getBody(), String.class);

        setMailHeader(whiteList.filter(message.getTos()), TO, out);
        setMailHeader(whiteList.filter(message.getCcs()), CC, out);
        setMailHeader(whiteList.filter(message.getBccs()), BCC, out);

        out.setHeader(REPLY_TO, message.getReplyTo());
        out.setHeader(FROM, message.getFrom());
        out.setHeader(SUBJECT, message.getSubject() != null ? getSubjectPrefix() + message.getSubject() : getSubjectPrefix());
        out.setHeader(CONTENT_TYPE, message.getContentType());
    }

    /**
     * Sets headerField if value is not the empty string
     *
     * @param source
     * @param headerField
     * @param out
     */
    private void setMailHeader(List<InternetAddress> source, String headerField, Message out) {

        String headerValue = Joiner.on(WhitelistChecker.SPLIT_CHAR).skipNulls().join(source);

        if (headerValue != null && !headerValue.equals("")) {
            out.setHeader(headerField, headerValue);
        }

    }

    private static final Logger LOG = LoggerFactory.getLogger(XMLMessageMailerProcessor.class);
    public static final String TO = "to";
    public static final String CC = "cc";
    public static final String BCC = "bcc";
    public static final String FROM = "from";
    public static final String REPLY_TO = "replyTo";
    public static final String SUBJECT = "subject";
    public static final String CONTENT_TYPE = "contentType";
    private WhitelistChecker whiteList;

    public WhitelistChecker getWhiteList() {
        return whiteList;
    }

    public void setWhiteList(WhitelistChecker whiteList) {
        this.whiteList = whiteList;
    }

    public String getSubjectPrefix() {
        return subjectPrefix;
    }

    public void setSubjectPrefix(String subjectPrefix) {
        this.subjectPrefix = subjectPrefix;
    }
}
