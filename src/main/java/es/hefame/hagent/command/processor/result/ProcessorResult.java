package es.hefame.hagent.command.processor.result;

import es.hefame.hagent.configuration.CONF;
import es.hefame.hcore.prtg.PrtgChannelResult;
import es.hefame.hcore.prtg.PrtgSensor;
import es.hefame.hcore.prtg.PrtgThresshold;

public abstract class ProcessorResult extends PrtgSensor
{
	// Carga de CPU
	protected double load_percentage = 0;

	public double get_load_percentage()
	{
		return load_percentage;
	}

	protected void channelize()
	{
		// Thressholds
		PrtgThresshold processor_load_percentage_thresshold = new PrtgThresshold(null, null, CONF.prtg.processor.warn_percent, CONF.prtg.processor.error_percent);

		// Uso de CPU
		this.addChannel(new PrtgChannelResult("Uso de CPU", this.get_load_percentage(), "Percent", processor_load_percentage_thresshold));
	}

}
