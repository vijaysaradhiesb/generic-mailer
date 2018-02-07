package com.integ.integration.mailer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.mail.internet.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class WhitelistChecker {
	
    private static final Logger LOG = LoggerFactory.getLogger(WhitelistChecker.class);
	
	static final String SPLIT_CHAR = ",";
	
	static final String EMAIL_SPLIT_CHAR = "@";
	
	List<InternetAddress> acceptedEmailSuffixes = null;
	
	List<String> acceptedDomainSuffixes = new ArrayList<String>();
	
	List<InternetAddress> emailOverride = null;

	public WhitelistChecker(String emailsSuffixesCommaSeparated, 
							String domainsSuffixesCommaSeparated,
							String emailOverride) throws AddressException {
		LOG.debug("Creating WhitelistChecker comma separeted email list:" + emailsSuffixesCommaSeparated);
		LOG.debug("Creating WhitelistChecker comma separeted domain list:" + domainsSuffixesCommaSeparated);
		
		acceptedEmailSuffixes = Arrays.asList(InternetAddress.parse(emailsSuffixesCommaSeparated, true));
		
		acceptedDomainSuffixes = Arrays.asList(domainsSuffixesCommaSeparated.toLowerCase().split(SPLIT_CHAR));
		
		this.emailOverride = Arrays.asList(InternetAddress.parse(emailOverride, true));
		
		// validate emails according to RFC822, as parse is not
		for(InternetAddress email : acceptedEmailSuffixes) {
				email.validate();
		}
		// validate email override according to RFC822, as parse is not
		for(InternetAddress email : this.emailOverride) {
			email.validate();
		}
		
		LOG.info("Creating Emails WhitelistChecker for:" + acceptedEmailSuffixes);
		LOG.info("Creating Domains WhitelistChecker for:" + acceptedDomainSuffixes);
		LOG.info("Creating WhitelistChecker using email override: {}", emailOverride);
	}
	
	private boolean check(InternetAddress internetAddress) {
		
		// validate email according to RFC822
		try {
			internetAddress.validate();
		} catch (AddressException ex) {
			LOG.error("Invalid email address is REMOVED by WhiteList: {} - error message: {}", internetAddress , ex.getMessage());
			return false;
		}
		
		if(acceptedEmailSuffixes.contains(internetAddress))
			return true;
		
		final String emailDomain = extractEmailSuffix(internetAddress);
		int count = Iterables.size(Iterables.filter(acceptedDomainSuffixes, new Predicate<String>() {
			@Override
			public boolean apply(String arg0) {
				return emailDomain.equalsIgnoreCase(arg0);
			}
	    }));
		// the email domain is included in the allowed ones
		if(count > 0)
			return true;
		
		return false;	
	}
	
	private String extractEmailSuffix(InternetAddress email) {
		// the email has always @ as it is already validated and cannot be empty or null
		String emailPart = email.getAddress();
		return emailPart.split(EMAIL_SPLIT_CHAR)[1];
		
	}
	
	private boolean isWhiteListEmpty(List<?> test) {
		if(test == null)
			return true;
		
		if(test.size() == 0)
			return true;
		
		if(test.size() == 1 && test.get(0).equals(""))
			return true;
		
		return false;
	}
	
	/**
	 * This method replaces each email in List emailsToCheck with the "email override address",
	 * if they are not allowed by the WhiteFilter.
	 * If "email override address" is not set then it removes the email completely.
	 */
	public List<InternetAddress> filter(List<String> emailsToCheck) throws AddressException {
		
		// do not allow duplicates
		Set<InternetAddress> filterList = new HashSet<InternetAddress>();
		
		List<InternetAddress> emails =  Arrays.asList(InternetAddress.parse(Joiner.on(SPLIT_CHAR).skipNulls().join(emailsToCheck), true));
		
		// if both white lists are empty AND no emailOverride defined, then accept all e-mails
		if(isWhiteListEmpty(acceptedEmailSuffixes) && isWhiteListEmpty(acceptedDomainSuffixes) && isWhiteListEmpty(emailOverride)){
			LOG.info("ALL emails allowed by WhiteList");
			return emails;
		}
			
		
		for(InternetAddress email : emails) {
			
			LOG.debug("Mailer whitelist filter is case insensitive");
//			String emailToCheck = email.getAddress().toLowerCase();
			
			if(check(email)) {
				
				LOG.info("Email WILL be used: {}", email);
				filterList.add(email);
				continue;
			}
			
			if(!isWhiteListEmpty(emailOverride)) {
				LOG.info("Email: {}, was REPLACED by whitelist with email: {}", email, emailOverride);
				filterList.addAll(emailOverride);
				continue;
			}
			LOG.info("Email: {}, was REMOVED by whitelist", email);
	
		}
		
		return new ArrayList<InternetAddress>(filterList);
		
	}

}
