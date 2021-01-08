package es.hefame.hagent.util.agent;

import java.util.Map.Entry;

import org.json.simple.JSONObject;

import es.hefame.hcore.JsonEncodable;
import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.util.OperatingSystem;

public class AgentInfoMessage implements JsonEncodable
{

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject jsonEncode()
	{

		JSONObject environment = new JSONObject();
		environment.putAll(System.getenv());

		JSONObject root = new JSONObject();
		root.put("agent", agent());
		root.put("os", env_os());
		root.put("runtime", runtime());
		root.put("environment", environment);

		JSONObject props = new JSONObject();
		for (Entry<Object, Object> prop : System.getProperties().entrySet())
		{
			props.put(prop.getKey().toString().replaceAll("\\.|\\-", "_"), prop.getValue().toString());
		}
		root.put("properties", props);

		JSONObject env = new JSONObject();
		for (Entry<String, String> e : System.getenv().entrySet())
		{
			env.put(e.getKey().replaceAll("\\.|\\-", "_"), e.getValue());
		}
		root.put("environment", env);

		return root;
	}

	private JSONObject env_os()
	{
		return OperatingSystem.get_os().jsonEncode();
	}

	@SuppressWarnings("unchecked")
	private JSONObject runtime()
	{
		JSONObject jvm = new JSONObject();
		jvm.put("mem_used", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
		jvm.put("mem_total", Runtime.getRuntime().totalMemory());
		jvm.put("mem_free", Runtime.getRuntime().freeMemory());
		jvm.put("mem_max", Runtime.getRuntime().maxMemory());
		jvm.put("processors", Runtime.getRuntime().availableProcessors());

		return jvm;
	}

	@SuppressWarnings("unchecked")
	private JSONObject agent()
	{
		JSONObject agent = new JSONObject();
		agent.put("uptime", AgentInfo.get_uptime());
		agent.put("version", agent_version());
		agent.put("port", CONF.agent.port);
		agent.put("config_file", CONF.get_config_file());
		agent.put("hostname", AgentInfo.get_fqdn_hostname());
		return agent;
	}

	@SuppressWarnings("unchecked")
	private JSONObject agent_version()
	{
		JSONObject jvm = new JSONObject();
		jvm.put("version", AgentInfo.get_version());
		jvm.put("build", AgentInfo.get_build());
		jvm.put("built_date", AgentInfo.get_built_date());

		return jvm;
	}

}
