package es.hefame.hagent.command.memory.result;

import es.hefame.hagent.configuration.CONF;
import es.hefame.hcore.prtg.PrtgChannelResult;
import es.hefame.hcore.prtg.PrtgSensor;
import es.hefame.hcore.prtg.PrtgThresshold;

public abstract class MemoryResult extends PrtgSensor
{

	// Kilobytes
	protected long		ram_total_bytes				= 0;
	protected long		ram_used_bytes				= 0;
	protected long		ram_free_bytes				= 0;
	protected double	ram_used_bytes_percentage	= 100;

	protected long		swap_total_bytes			= 0;
	protected long		swap_used_bytes				= 0;
	protected long		swap_free_bytes				= 0;
	protected double	swap_used_bytes_percentage	= 100;

	public long get_ram_total_bytes()
	{
		return ram_total_bytes;
	}

	public long get_ram_used_bytes()
	{
		return ram_used_bytes;
	}

	public long get_ram_free_bytes()
	{
		return ram_free_bytes;
	}

	public double get_ram_used_bytes_percentage()
	{
		return ram_used_bytes_percentage;
	}

	public long get_swap_total_bytes()
	{
		return swap_total_bytes;
	}

	public long get_swap_used_bytes()
	{
		return swap_used_bytes;
	}

	public long get_swap_free_bytes()
	{
		return swap_free_bytes;
	}

	public double get_swap_used_bytes_percentage()
	{
		return swap_used_bytes_percentage;
	}

	protected void channelize()
	{
		// Thressholds
		PrtgThresshold ram_used_percentage_thresshold = new PrtgThresshold(null, null, CONF.prtg.memory.physical_warn_percent, CONF.prtg.memory.physical_error_percent);
		PrtgThresshold swap_used_percentage_thresshold = new PrtgThresshold(null, null, CONF.prtg.memory.swap_warn_percent, CONF.prtg.memory.swap_error_percent);

		// Memoria Fisica
		this.addChannel(new PrtgChannelResult("Uso de memoria fisica", this.get_ram_used_bytes_percentage(), "Percent", ram_used_percentage_thresshold));
		this.addChannel(new PrtgChannelResult("Memoria fisica usada", this.get_ram_used_bytes(), "BytesDisk"));
		this.addChannel(new PrtgChannelResult("Memoria fisica total", this.get_ram_total_bytes(), "BytesDisk"));

		// SWAP
		this.addChannel(new PrtgChannelResult("Uso de SWAP", this.get_swap_used_bytes_percentage(), "Percent", swap_used_percentage_thresshold));
		this.addChannel(new PrtgChannelResult("SWAP usada", this.get_swap_used_bytes(), "BytesDisk"));
		this.addChannel(new PrtgChannelResult("SWAP total", this.get_swap_total_bytes(), "BytesDisk"));

	}

}
