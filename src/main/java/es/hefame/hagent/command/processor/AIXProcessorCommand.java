package es.hefame.hagent.command.processor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hagent.command.processor.result.AIXProcessorResult;
import es.hefame.hagent.configuration.CONF;

public class AIXProcessorCommand extends ProcessorCommand
{
	private static Logger		L						= LogManager.getLogger();
	private static final Marker	PROCESSOR_CMD_MARKER	= MarkerManager.getMarker("CPU_CMD");

	@Override
	public AIXProcessorResult operate()
	{
		try
		{
			int min_sample_time = CONF.prtg.processor.sample_time;
			String sample_time = "" + min_sample_time;

			OsCommandExecutor c = new OsCommandExecutor(PROCESSOR_CMD_MARKER, "lparstat", sample_time, "1");
			OsCommandResult lsparstat_result = c.run();
			
			c = new OsCommandExecutor(PROCESSOR_CMD_MARKER, "vmstat", "-WI", "1", "1");
			OsCommandResult vmstat_result = c.run();
			
			
			AIXProcessorResult result = new AIXProcessorResult(lsparstat_result, vmstat_result);
			return result;
		}
		catch (Exception e)
		{
			L.catching(e);
			return null;
		}
	}

}
