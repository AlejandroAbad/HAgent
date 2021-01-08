package es.hefame.hagent.command.os.updates.result;

import org.json.simple.JSONObject;

import es.hefame.hcore.JsonEncodable;

public class OsUpdatesResult implements JsonEncodable
{
	protected int updates;

	public OsUpdatesResult()
	{
		this.updates = -1;
	}

	public OsUpdatesResult(int updates)
	{
		this.updates = updates;
	}

	public int get_available_updates()
	{
		return this.updates;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject jsonEncode()
	{
		JSONObject root = new JSONObject();
		root.put("updates", this.updates);
		return root;

	}

}
