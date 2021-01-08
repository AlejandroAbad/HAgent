package es.hefame.hagent.util.mail;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.util.agent.AgentInfo;

public class Mailer
{
	private static Logger			L			= LogManager.getLogger();
	private static final Marker		MARKER		= MarkerManager.getMarker("MAILER");

	private String					smtp_server	= null;
	private int						smtp_port	= 25;
	private String					smtp_user	= null;
	private String					smtp_pass	= null;
	private boolean					smtp_tls	= false;
	private String					mail_from	= null;
	private String					reply_to	= null;
	private boolean					debug		= false;

	private String					to			= "";
	private String					subject		= "";
	private String					body		= "";
	private String					html		= "";
	private String					from_name	= "";
	private List<EmailAttachment>	attachments	= new LinkedList<EmailAttachment>();

	public Mailer()
	{
		smtp_server = CONF.mailer.smtp_server;
		smtp_port = CONF.mailer.smtp_port;
		smtp_user = CONF.mailer.smtp_user;
		smtp_pass = CONF.mailer.smtp_pass;
		smtp_tls = CONF.mailer.smtp_tls;
		mail_from = CONF.mailer.mail_from;
		reply_to = CONF.mailer.reply_to;
		from_name = AgentInfo.get_hostname().toUpperCase();
	}

	public Mailer from(String from_name)
	{
		this.from_name = from_name;
		return this;
	}

	public Mailer to(String to)
	{
		this.to = to;
		return this;
	}

	public Mailer subject(String subject)
	{
		this.subject = subject;
		return this;
	}

	public Mailer setDebug(boolean debug)
	{
		this.debug = debug;
		return this;
	}

	public Mailer body(String body)
	{
		this.body = body;
		return this;
	}

	public Mailer html(String body)
	{
		this.html = body;
		return this;
	}

	public Mailer attach(String filepath, String name, String description)
	{
		EmailAttachment attachment = new EmailAttachment();
		attachment.setPath(filepath);
		attachment.setDisposition(EmailAttachment.ATTACHMENT);
		attachment.setDescription(description);
		attachment.setName(name);
		this.attachments.add(attachment);
		return this;
	}

	public Mailer send()
	{
		if (L.isDebugEnabled(MARKER))
		{
			L.debug(MARKER, "Enviando correo electronico con los siguientes parametros");
			L.debug(MARKER, "smtp_server = {}", smtp_server);
			L.debug(MARKER, "smtp_port = {}", smtp_port);
			L.debug(MARKER, "smtp_user = {}", smtp_user);
			L.debug(MARKER, "smtp_pass = *******");
			L.debug(MARKER, "smtp_tls = {}", smtp_tls);
			L.debug(MARKER, "mail_from = {}", mail_from);
			L.debug(MARKER, "reply_to = {}", reply_to);
			L.debug(MARKER, "from_name = {}", from_name);
		}

		try
		{
			Email email;

			if (this.attachments.isEmpty())
			{
				if (this.html.length() > 0)
				{
					L.debug(MARKER, "Es un mail en HTML:\n{}", this.html);
					HtmlEmail html_email = new HtmlEmail();
					html_email.setHtmlMsg(this.html);
					email = html_email;
				}
				else
				{
					L.debug(MARKER, "Es un mail en texto plano:\n{}", this.body);
					email = new SimpleEmail();
					email.setMsg(this.body);
				}
			}
			else
			{
				L.debug(MARKER, "Es un mail en texto plano:\n{}", this.body);
				MultiPartEmail mp_email = new MultiPartEmail();
				for (EmailAttachment attachment : this.attachments)
				{
					L.debug(MARKER, "Adjunto: {}", attachment.toString());
					mp_email.attach(attachment);
				}
				email = mp_email;
				email.setMsg(this.body);
			}

			email.setDebug(this.debug);
			email.setHostName(this.smtp_server);
			email.setSmtpPort(this.smtp_port);

			if (this.smtp_user != null && this.smtp_pass != null)
			{
				email.setAuthenticator(new DefaultAuthenticator(this.smtp_user, this.smtp_pass));
			}

			email.setStartTLSEnabled(this.smtp_tls);
			email.setFrom(this.mail_from, this.from_name);
			email.setSubject(this.subject);
			email.addReplyTo(this.reply_to);
			email.addTo(this.to);

			email.send();
			L.debug("Es un mail con Adjuntos");

		}
		catch (EmailException e)
		{
			L.error(MARKER, "Error al enviar el correo");
			L.catching(e);
		}
		return this;
	}
}
