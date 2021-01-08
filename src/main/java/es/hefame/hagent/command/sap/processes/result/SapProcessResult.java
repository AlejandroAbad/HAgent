package es.hefame.hagent.command.sap.processes.result;

import es.hefame.hcore.prtg.PrtgChannelResult;
import es.hefame.hcore.prtg.PrtgErrorResult;
import es.hefame.hcore.prtg.PrtgSensor;

public class SapProcessResult extends PrtgSensor
{
	// private static Logger L = LogManager.getLogger();
	// private static final Marker SAP_PROCESS_CMD_MARKER = MarkerManager.getMarker("SAP_PROCESS_CMD");

	public static final int	STATUS_OK		= 1;
	public static final int	STATUS_ERROR	= 3;
	public static final int	STATUS_WARN		= 2;
	public static final int	STATUS_UNKNOWN	= 5;

	private String			name;
	private String			description;
	private int				status_value;

	public SapProcessResult(String[] data)
	{
		if (data.length < 3)
		{
			this.addChannel(new PrtgErrorResult("No se pudo obtener informaci�n del proceso SAP."));
			return;
		}

		this.name = data[0].trim();
		this.description = data[1].trim();
		switch (data[2].trim().toLowerCase())
		{
			case "green":
				this.status_value = STATUS_OK;
				break;
			case "yellow":
				this.status_value = STATUS_WARN;
				break;
			default:
				this.status_value = STATUS_ERROR;
				break;
		}

		this.channelize();
	}

	public String get_name()
	{
		return name;
	}

	public String get_description()
	{
		return description;
	}

	public int get_status_value()
	{
		return status_value;
	}

	protected void channelize()
	{
		PrtgChannelResult channel = new PrtgChannelResult(this.description + " (" + this.name + ")", this.status_value, "Custom");
		channel.setValueLookup("prtg.standardlookups.cisco.ciscoenvmonstate");
		this.addChannel(channel);
	}
}
