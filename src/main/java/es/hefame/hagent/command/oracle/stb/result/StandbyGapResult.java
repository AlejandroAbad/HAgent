package es.hefame.hagent.command.oracle.stb.result;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import es.hefame.hcore.prtg.PrtgChannelResult;
import es.hefame.hcore.prtg.PrtgErrorResult;
import es.hefame.hcore.prtg.PrtgResult;
import es.hefame.hcore.prtg.PrtgSensor;

public class StandbyGapResult extends PrtgSensor
{

	private class SequencePair
	{
		private Long	stbSeq		= null;
		private Long	primarySeq	= null;

		private long getDiff()
		{
			return primarySeq - stbSeq;
		}

		private boolean isFilled()
		{
			return (stbSeq != null && primarySeq != null);
		}
	}

	private Map<Integer, SequencePair> sequences = new HashMap<Integer, SequencePair>();

	public void addSequence(int thread, long sequence, boolean forStandby)
	{
		SequencePair pair = sequences.get(thread);
		if (pair == null)
		{
			pair = new SequencePair();
			sequences.put(thread, pair);
		}

		if (forStandby)
		{
			pair.stbSeq = sequence;
		}
		else
		{
			pair.primarySeq = sequence;
		}

	}

	public StandbyGapResult closeSensor()
	{

		Iterator<Entry<Integer, SequencePair>> it = this.sequences.entrySet().iterator();

		while (it.hasNext())
		{

			Entry<Integer, SequencePair> entry = it.next();
			Integer thread = entry.getKey();
			SequencePair sequence = entry.getValue();

			PrtgResult channel = null;
			if (sequence.isFilled())
			{
				channel = new PrtgChannelResult("Thread #" + thread, sequence.getDiff(), "seq");
			}
			else
			{
				channel = new PrtgErrorResult("No se pudieron obtener los numeros de secuencia del thread #" + thread);
			}
			this.addChannel(channel);
		}

		return this;

	}

}
