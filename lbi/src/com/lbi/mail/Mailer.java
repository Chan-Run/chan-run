package com.lbi.mail;

import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailConstants;
import org.apache.commons.mail.HtmlEmail;

import org.apache.commons.validator.routines.EmailValidator;

import com.lbi.framework.app.Request;

public class Mailer
{
	
	private static EmailValidator validator = EmailValidator.getInstance();
	
	public static void send(String to, String from, String replyTo, List<String> cc, List<String> bcc, String subject, String msg, String footer, Request req) throws Exception
	{
		validateMailIds(to, from, replyTo);
		HtmlEmail mail = getMailClient(req);
		mail.addTo(to);
		mail.setFrom(from);
		mail.addReplyTo(replyTo);
		mail.setSubject(subject);
		mail.setMsg(msg + "<br><br>" + footer);
		
		addCc(mail, cc);
		addBcc(mail, bcc);
		
		mail.send();
	}
	
	public static void sendWithAttachment(String to, String from, String replyTo, List<String> cc, List<String> bcc, String subject, String msg, String footer, Request req, List<String> attachments) throws Exception
	{
		validateMailIds(to, from, replyTo);
		HtmlEmail mail = getMailClient(req);
		mail.addTo(to);
		mail.setFrom(from);
		mail.addReplyTo(replyTo);
		mail.setSubject(subject);
		mail.setMsg(msg + "<br><br>" + footer);
		
		addCc(mail, cc);
		addBcc(mail, bcc);
		
		addAttchments(mail, attachments);
		
		mail.send();

	}
	
	public static void sendToSupport(String sub, String msg, Request req) throws Exception
	{
//		String supportMailId = req.getDomainMap().getSupportMailId();
		String supportMailId = "support@ladderbi.com";
		if(validator.isValid(supportMailId))
		{
			throw new Exception("Invalid Support Mail Id");
		}
		HtmlEmail mail = getMailClient(req);
		mail.addTo(supportMailId);
		mail.setSubject(sub);
		mail.setMsg(msg);
		mail.send();
	}
	
	public static HtmlEmail getMailClient(Request req)
	{
		HtmlEmail mail = new HtmlEmail();
		configureProps(mail, req);
		return mail;
	}
	
	public static void addCc(HtmlEmail mail, List<String> cc) throws Exception
	{
		List<InternetAddress> ccList = new ArrayList<InternetAddress>();
		for(String mailId : cc)
		{
			if(!validator.isValid(mailId))
			{
				System.out.println("Invalid Email Id in CC List");
				continue;
			}
			ccList.add(new InternetAddress(mailId));
		}
		
		mail.setCc(ccList);
	}
	
	public static void addBcc(Email mail, List<String> cc) throws Exception
	{
		List<InternetAddress> bccList = new ArrayList<InternetAddress>();
		for(String mailId : cc)
		{
			if(!validator.isValid(mailId))
			{
				System.out.println("Invalid Email Id in BCC List");
				continue;
			}
			bccList.add(new InternetAddress(mailId));
		}
		
		mail.setBcc(bccList);
	}
	
	public static void configureProps(HtmlEmail mail, Request req)
	{
		mail.setAuthenticator(new DefaultAuthenticator("", ""));
		mail.setSSLOnConnect(true);
		mail.setCharset(EmailConstants.UTF_8);
		mail.setHostName("");
		mail.setSmtpPort(465);
		mail.setStartTLSEnabled(true);
		mail.setStartTLSRequired(true);
		mail.setBounceAddress("support@ladderbi.com");
		mail.setSendPartial(true);
	}
	
	public static void validateMailIds(String to, String from, String replyTo) throws Exception
	{
		if(!validator.isValid(to))
		{
			throw new Exception("Invalid To: Email Id");
		}
		
		if(!validator.isValid(from))
		{
			throw new Exception("Invalid From: Email Id");
		}
		
		if(!validator.isValid(replyTo))
		{
			throw new Exception("Invalid ReplyTo: Email Id");
		}
	}
	
	public static void addAttchments(HtmlEmail mail, List<String> attachments) throws Exception
	{
		EmailAttachment att;
		
		for(String fileName : attachments)
		{
			att = new EmailAttachment();
			att.setPath(fileName);
			att.setName(fileName);
			att.setDisposition(EmailAttachment.ATTACHMENT);
			mail.attach(att);
		}
	}
	
}
