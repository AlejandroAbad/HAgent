package es.hefame.hagent.command.filesystems.result;

import es.hefame.hcore.converter.DiskSizeConverter;
import es.hefame.hcore.prtg.PrtgErrorResult;

public class LNXFilesystemResult extends FilesystemResult
{
	protected String device;

	public LNXFilesystemResult(String[] data)
	{
		if (data.length != 6)
		{
			// this.set_in_error("El numero de tokens es incorrecto [" + data.length + " != 6]");
			this.addChannel(new PrtgErrorResult("No se pudo obtener informaci�n de filesystem."));
			return;
		}

		this.device = data[0];
		this.mount_point = data[5];

		try
		{
			this.total_bytes = Long.parseLong(data[1]) * DiskSizeConverter.SizeFactor.KILO.factor;
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error(this.mount_point + " - Al obtener el numero total de bytes no se pudo convertir [" + data[1] + "] a long: " + e.getMessage() + ".");
			this.addChannel(new PrtgErrorResult("No se pudo obtener informaci�n de filesystem."));
			return;
		}

		try
		{
			this.used_bytes = Long.parseLong(data[2]) * DiskSizeConverter.SizeFactor.KILO.factor;
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error(this.mount_point + " - Al obtener el numero de bytes usados no se pudo convertir [" + data[2] + "] a long: " + e.getMessage() + ".");
			this.addChannel(new PrtgErrorResult("No se pudo obtener informaci�n de filesystem."));
			return;
		}

		try
		{
			this.free_bytes = Long.parseLong(data[3]) * DiskSizeConverter.SizeFactor.KILO.factor;
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error(this.mount_point + " - Al obtener el numero de bytes libres no se pudo convertir [" + data[3] + "] a long: " + e.getMessage() + ".");
			this.addChannel(new PrtgErrorResult("No se pudo obtener informaci�n de filesystem."));
			return;
		}
		try
		{
			this.used_bytes_percentage = Double.parseDouble(data[4].substring(0, data[4].length() - 1));
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error(this.mount_point + " - Al obtener el porcentaje usado no se pudo convertir [" + data[4] + "] a double: " + e.getMessage() + ".");
			this.addChannel(new PrtgErrorResult("No se pudo obtener informaci�n de filesystem."));
			return;
		}

		this.channelize();

	}

	public String get_device()
	{
		return device;
	}

}
