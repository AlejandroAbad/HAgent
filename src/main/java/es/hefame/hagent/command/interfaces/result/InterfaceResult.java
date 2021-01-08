package es.hefame.hagent.command.interfaces.result;

import es.hefame.hcore.prtg.PrtgChannelResult;
import es.hefame.hcore.prtg.PrtgSensor;

public abstract class InterfaceResult extends PrtgSensor
{
	protected String	interface_name;
	protected long		tx_bytes_per_second;
	protected long		rx_bytes_per_second;
	protected long		total_bytes_per_second;

	public String get_interface_name()
	{
		return interface_name;
	}

	public long get_tx_bytes_per_second()
	{
		return tx_bytes_per_second;
	}

	public long get_rx_bytes_per_second()
	{
		return rx_bytes_per_second;
	}

	public long get_total_bytes_per_second()
	{
		return total_bytes_per_second;
	}

	protected void channelize()
	{
		this.addChannel(new PrtgChannelResult("Tasa de transmision en '" + this.get_interface_name() + "'", this.get_tx_bytes_per_second(), "SpeedNet"));
		this.addChannel(new PrtgChannelResult("Tasa de recepcion en '" + this.get_interface_name() + "'", this.get_rx_bytes_per_second(), "SpeedNet"));
	}

}
