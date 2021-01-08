package es.hefame.hagent.command.filesystems.result;

import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.configuration.prtg.PrtgFilesystemsConfiguration.FilesystemConfigData;
import es.hefame.hcore.prtg.PrtgChannelResult;
import es.hefame.hcore.prtg.PrtgSensor;
import es.hefame.hcore.prtg.PrtgThresshold;

public abstract class FilesystemResult extends PrtgSensor
{

	protected String	mount_point;
	protected long		total_bytes				= 0;
	protected long		used_bytes				= 0;
	protected long		free_bytes				= 0;
	protected double	used_bytes_percentage	= 100;

	public String get_mount_point()
	{
		return mount_point;
	}

	public long get_total_bytes()
	{
		return total_bytes;
	}

	public long get_used_bytes()
	{
		return used_bytes;
	}

	public long get_free_bytes()
	{
		return free_bytes;
	}

	public double get_used_bytes_percentage()
	{
		return used_bytes_percentage;
	}

	public double get_free_bytes_percentage()
	{
		return 100 - used_bytes_percentage;
	}

	protected void channelize()
	{

		FilesystemConfigData config_data = CONF.prtg.filesystems.get(this.get_mount_point());

		PrtgThresshold used_bytes_percent_thresshold = null;
		PrtgThresshold free_bytes_thresshold = null;

		if (config_data != null)
		{
			used_bytes_percent_thresshold = new PrtgThresshold(null, null, config_data.get_warn_percent(), config_data.get_error_percent());
			free_bytes_thresshold = new PrtgThresshold(config_data.get_error_free(), config_data.get_warn_free(), null, null);
		}

		this.addChannel(new PrtgChannelResult("Uso de " + this.get_mount_point(), this.get_used_bytes_percentage(), "Percent", used_bytes_percent_thresshold));
		// this.addChannel(new PrtgChannelResult("Bytes usados en " + this.get_mount_point(), this.get_used_bytes(), "BytesDisk"));
		this.addChannel(new PrtgChannelResult("Bytes libres en " + this.get_mount_point(), this.get_free_bytes(), "BytesDisk", free_bytes_thresshold));
		this.addChannel(new PrtgChannelResult("Bytes total en " + this.get_mount_point(), this.get_total_bytes(), "BytesDisk"));

	}

}
