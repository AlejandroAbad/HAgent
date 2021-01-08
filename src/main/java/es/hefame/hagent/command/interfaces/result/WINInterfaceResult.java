package es.hefame.hagent.command.interfaces.result;

import es.hefame.hcore.prtg.PrtgErrorResult;

public class WINInterfaceResult extends InterfaceResult
{
	/*
	 * FORMATO ESPERADO
	 * *******************************************************************************************
	 * 
	 * isatap.{94609864-2C0B-442F-928B-FCDC0EA572F3} 0 0
	 * isatap.hefame.es 0 0
	 * Realtek PCIe GBE Family Controller 19897 15153
	 *
	 *
	 * *******************************************************************************************
	 */

	public WINInterfaceResult(String[] data)
	{

		if (data.length < 3)
		{
			// this.set_in_error("El numero de tokens es incorrecto [" + data.length + " < 3]");
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		this.interface_name = data[0];
		for (int i = 1; i < data.length - 2; i++)
		{
			this.interface_name += ' ' + data[i];
		}

		this.interface_name = this.interface_name.trim();

		try
		{
			this.rx_bytes_per_second = Long.parseLong(data[data.length - 2]);
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error(this.interface_name + " - Al obtener el numero de bytes transmitidos no se pudo convertir [" + data[1] + "] a long: " + e.getMessage());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		try
		{
			this.tx_bytes_per_second = Long.parseLong(data[data.length - 1]);
		}
		catch (NumberFormatException e)
		{
			// this.set_in_error(this.interface_name + " - Al obtener el numero de bytes recividos no se pudo convertir [" + data[1] + "] a long: " + e.getMessage());
			this.addChannel(new PrtgErrorResult("No se pudo obtener la memoria."));
			return;
		}

		this.total_bytes_per_second = this.rx_bytes_per_second + this.tx_bytes_per_second;

		this.channelize();
	}

}
