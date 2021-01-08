package es.hefame.hagent.command.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.command.processor.result.LNXProcessorResult;
import es.hefame.hagent.configuration.CONF;

public class LNXProcessorCommand extends ProcessorCommand
{
	private static Logger		L						= LogManager.getLogger();
	private static final Marker	PROCESSOR_CMD_MARKER	= MarkerManager.getMarker("CPU_CMD");

	@Override
	public LNXProcessorResult operate()
	{
		try
		{
			int min_sample_time = CONF.prtg.processor.sample_time;
			String sample_time = "" + min_sample_time;

			OsCommandExecutor c = new OsCommandExecutor(PROCESSOR_CMD_MARKER, "vmstat", sample_time, "2");
			OsCommandResult command_result = c.run();
			return new LNXProcessorResult(command_result);
		}
		catch (Exception e)
		{
			L.catching(e);
			return null;
		}

	}

}
