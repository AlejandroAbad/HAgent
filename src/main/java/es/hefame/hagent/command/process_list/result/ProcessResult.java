package es.hefame.hagent.command.process_list.result;

import org.json.simple.JSONObject;

import es.hefame.hcore.JsonEncodable;

public abstract class ProcessResult implements JsonEncodable
{
	protected String	uid;
	protected int		pid;
	protected int		ppid;
	protected String	start_date;
	protected long		cpu_time;
	protected String	cmd;

	public String get_uid()
	{
		return uid;
	}

	public int get_pid()
	{
		return pid;
	}

	public int get_ppid()
	{
		return ppid;
	}

	public String get_start_date()
	{
		return start_date;
	}

	public long get_cpd_time()
	{
		return cpu_time;
	}

	public String get_cmd()
	{
		return cmd;
	}

	@SuppressWarnings("unchecked")
	public JSONObject jsonEncode()
	{
		JSONObject root = new JSONObject();
		root.put("uid", uid);
		root.put("pid", pid);
		root.put("ppid", ppid);
		root.put("start_date", start_date);
		root.put("cpu_time", cpu_time);
		root.put("cmd", cmd);
		return root;
	}

	public String toString()
	{
		return this.jsonEncode().toJSONString();
	}

}
