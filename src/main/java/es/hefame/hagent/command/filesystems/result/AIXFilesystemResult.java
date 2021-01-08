package es.hefame.hagent.command.filesystems.result;

import es.hefame.hcore.converter.DiskSizeConverter;
import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.configuration.prtg.PrtgFilesystemsConfiguration.FilesystemConfigData;
import es.hefame.hcore.prtg.PrtgChannelResult;
import es.hefame.hcore.prtg.PrtgErrorResult;
import es.hefame.hcore.prtg.PrtgThresshold;

public class AIXFilesystemResult extends FilesystemResult
{
	protected String	device;
	// Inodos
	private long		used_inodes				= 0;
	private long		free_inodes				= 0;
	private double		used_inodes_percentage	= 100;

	public AIXFilesystemResult(String[] data)
	{
		if (data.length != 9)
		{
			// this.set_in_error("El numero de tokens es incorrecto [" + data.length + " != 9]");
			this.addChannel(new PrtgErrorResult("No se pudo obtener informacion de filesystem."));
			return;
		}

		this.device = data[0];
		this.mount_point = data[1];

		try
		{
			this.total_bytes = Long.parseLong(data[2]) * DiskSizeConverter.SizeFactor.KILO.factor;
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error(this.mount_point + " - Al obtener el numero total de bytes no se pudo convertir [" + data[2] + "] a long: " + e.getMessage() + ".");
			this.addChannel(new PrtgErrorResult("No se pudo obtener informaci�n de filesystem."));
			return;
		}

		try
		{
			this.used_bytes = Long.parseLong(data[3]) * DiskSizeConverter.SizeFactor.KILO.factor;
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error(this.mount_point + " - Al obtener el numero de bytes usados no se pudo convertir [" + data[3] + "] a long: " + e.getMessage() + ".");
			this.addChannel(new PrtgErrorResult("No se pudo obtener informaci�n de filesystem."));
			return;
		}

		try
		{
			this.free_bytes = Long.parseLong(data[4]) * DiskSizeConverter.SizeFactor.KILO.factor;
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error(this.mount_point + " - Al obtener el numero de bytes libres no se pudo convertir [" + data[4] + "] a long: " + e.getMessage() + ".");
			this.addChannel(new PrtgErrorResult("No se pudo obtener informaci�n de filesystem."));
			return;
		}
		try
		{
			this.used_bytes_percentage = Double.parseDouble(data[5].substring(0, data[5].length() - 1));
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error(this.mount_point + " - Al obtener el porcentaje usado no se pudo convertir [" + data[5] + "] a double: " + e.getMessage() + ".");
			this.addChannel(new PrtgErrorResult("No se pudo obtener informaci�n de filesystem."));
			return;
		}
		try
		{
			this.used_inodes = Long.parseLong(data[6]);
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error(this.mount_point + " - Al obtener el numero total de inodos usados no se pudo convertir [" + data[6] + "] a long: " + e.getMessage() + ".");
			this.addChannel(new PrtgErrorResult("No se pudo obtener informaci�n de filesystem."));
			return;
		}
		try
		{
			this.free_inodes = Long.parseLong(data[7]);
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error(this.mount_point + " - Al obtener el numero total de inodos libres no se pudo convertir [" + data[7] + "] a long: " + e.getMessage() + ".");
			this.addChannel(new PrtgErrorResult("No se pudo obtener informaci�n de filesystem."));
			return;
		}
		try
		{
			this.used_inodes_percentage = Double.parseDouble(data[8].substring(0, data[8].length() - 1));
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error(this.mount_point + " - Al obtener el porcentaje de inodos usado no se pudo convertir [" + data[8] + "] a long: " + e.getMessage() + ".");
			this.addChannel(new PrtgErrorResult("No se pudo obtener informaci�n de filesystem."));
			return;
		}

		this.channelize();

		FilesystemConfigData config_data = CONF.prtg.filesystems.get(this.get_mount_point());

		if (config_data != null && config_data.is_check_inodes())
		{
			PrtgThresshold free_inodes_percent_thresshold = new PrtgThresshold(config_data.get_inodes_error_free_percent(), config_data.get_inodes_warn_free_percent(), null, null);
			this.addChannel(new PrtgChannelResult("Inodos libres en " + this.get_mount_point(), this.get_free_inodes_percentage(), "Percent", free_inodes_percent_thresshold));
		}

	}

	public String get_device()
	{
		return device;
	}

	public long get_used_inodes()
	{
		return used_inodes;
	}

	public long get_free_inodes()
	{
		return free_inodes;
	}

	public double get_used_inodes_percentage()
	{
		return used_inodes_percentage;
	}

	public double get_free_inodes_percentage()
	{
		return 100 - used_inodes_percentage;
	}

}
