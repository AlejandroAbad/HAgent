package es.hefame.hagent.util.agent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import es.hefame.hcore.JsonEncodable;
import es.hefame.hcore.http.HttpException;

public class AgentRegistrationMessage implements JsonEncodable
{
	private static Logger	L	= LogManager.getLogger();
	private String			hostname;
	private long			date_registered;

	public AgentRegistrationMessage(byte[] data) throws HttpException
	{
		try
		{
			JSONObject json_root = (JSONObject) JSONValue.parseWithException(new InputStreamReader(new ByteArrayInputStream(data)));
			if (json_root == null) { throw L.throwing(new HttpException(400, "No body in the response")); }

			Object o;

			// hostname
			o = json_root.get("hostname");
			if (o != null)
			{
				this.hostname = o.toString();
			}
			else
			{
				throw L.throwing(new HttpException(400, "No field 'hostname' in the response"));
			}

			// date_registered
			o = json_root.get("date_registered");
			if (o != null)
			{
				try
				{
					this.date_registered = Long.parseLong(o.toString());
				}
				catch (NumberFormatException e)
				{
					throw L.throwing(new HttpException(400, "Field 'date_registered' is not numeric"));
				}

			}
			else
			{
				throw L.throwing(new HttpException(400, "No field 'date_registered' in the response"));
			}

		}
		catch (IOException | org.json.simple.parser.ParseException e)
		{
			L.error("Excepcion al registrar el agente");
			L.catching(e);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject jsonEncode()
	{
		JSONObject json = new JSONObject();
		json.put("hostname", this.hostname);
		json.put("date_registered", this.date_registered);
		return json;
	}

}
