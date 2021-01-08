package es.hefame.hagent.command.filesystems.result;

import es.hefame.hcore.prtg.PrtgErrorResult;

public class WINFilesystemResult extends FilesystemResult
{
	protected String label;

	public WINFilesystemResult(String[] data)
	{
		if (data.length != 4)
		{
			// this.set_in_error("El numero de tokens es incorrecto [" + data.length + " != 4]");
			this.addChannel(new PrtgErrorResult("No se pudo obtener informaci�n de filesystem."));
			return;
		}

		this.mount_point = data[0];

		try
		{
			this.free_bytes = Long.parseLong(data[1]);
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error(this.mount_point + " - Al obtener el numero de bytes libres no se pudo convertir [" + data[1] + "] a long: " + e.getMessage() + ".");
			this.addChannel(new PrtgErrorResult("No se pudo obtener informaci�n de filesystem."));
			return;
		}

		try
		{
			this.total_bytes = Long.parseLong(data[2]);
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error(this.mount_point + " - Al obtener el numero total de bytes no se pudo convertir [" + data[2] + "] a long: " + e.getMessage() + ".");
			this.addChannel(new PrtgErrorResult("No se pudo obtener informaci�n de filesystem."));
			return;
		}

		this.used_bytes = this.total_bytes - this.free_bytes;
		this.used_bytes_percentage = (this.total_bytes > 0) ? (this.used_bytes * 100) / this.total_bytes : 0;
		this.label = data[3];

		this.channelize();

	}

	public String get_label()
	{
		return label;
	}

}
