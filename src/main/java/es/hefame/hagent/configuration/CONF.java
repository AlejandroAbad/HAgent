package es.hefame.hagent.configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import es.hefame.hcore.JsonEncodable;
import es.hefame.hcore.HException;
import es.hefame.hagent.configuration.agent.AgentConfiguration;
import es.hefame.hagent.configuration.alert_channel.AlertChannelConfiguration;
import es.hefame.hagent.configuration.core.ConfigurationReader;
import es.hefame.hagent.configuration.mailer.MailerConfiguration;
import es.hefame.hagent.configuration.monitorized_element.MonitorizedElementConfiguration;
import es.hefame.hagent.configuration.prtg.PrtgConfiguration;
import es.hefame.hagent.util.exception.CommandException;

public class CONF implements JsonEncodable
{
	public static CONF							instance		= null;
	public static AgentConfiguration		agent;
	public static MailerConfiguration		mailer;
	public static PrtgConfiguration			prtg;
	public static MonitorizedElementConfiguration		checker;
	public static AlertChannelConfiguration	channels;
	private ConfigurationReader				config_reader	= null;

	public static void load() throws HException
	{
		if (CONF.instance == null) CONF.instance = new CONF();
	}

	public static void reload() throws HException
	{
		if (CONF.instance != null) instance.config_reader.reload();
	}

	public static void stop_configuration_reloader()
	{
		if (CONF.instance != null) instance.config_reader.stop_configuration_reloader();
	}

	public static String get_config_file()
	{
		if (CONF.instance != null) return instance.config_reader.get_configuration_file();
		return null;
	}

	private CONF() throws HException
	{
		this.config_reader = new ConfigurationReader();
		CONF.agent = new AgentConfiguration(config_reader);
		CONF.mailer = new MailerConfiguration(config_reader);
		CONF.prtg = new PrtgConfiguration(config_reader);
		CONF.checker = new MonitorizedElementConfiguration(config_reader);
		CONF.channels = new AlertChannelConfiguration(config_reader);
		this.config_reader.reload();
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONAware jsonEncode()
	{
		JSONObject root = new JSONObject();
		root.put(AgentConfiguration.BASE_NODE_NAME, CONF.agent.jsonEncode());
		root.put(MailerConfiguration.BASE_NODE_NAME, CONF.mailer.jsonEncode());
		root.put(PrtgConfiguration.BASE_NODE_NAME, CONF.prtg.jsonEncode());
		root.put(MonitorizedElementConfiguration.BASE_NODE_NAME, CONF.checker.jsonEncode());
		root.put(AlertChannelConfiguration.BASE_NODE_NAME, CONF.channels.jsonEncode());
		return root;
	}

	public static void set_configuration_file_contents(byte[] new_data) throws CommandException
	{
		try
		{
			JSONObject root = (JSONObject) JSONValue.parseWithException(new String(new_data));

			File new_config_file = new File(instance.config_reader.get_configuration_file());

			Writer file_writer = new FileWriter(new_config_file, false);
			file_writer.write(root.toJSONString());
			file_writer.close();

			instance.config_reader.reload();

		}
		catch (ParseException e)
		{
			throw new CommandException("Excepcion [" + e.getClass().getName() + "] al interpretar la nueva configuracion. Mensaje: " + e.getMessage());
		}
		catch (IOException e)
		{
			throw new CommandException("Excepcion [" + e.getClass().getName() + "] al escribir la nueva configuracion. Mensaje: " + e.getMessage());
		}
	}

}
