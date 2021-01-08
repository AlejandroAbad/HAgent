package es.hefame.hagent.bg.sampler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.hagent.bg.RecurringOperation;
import es.hefame.hagent.command.Command;

public class Sampler extends RecurringOperation
{
	private static Logger	L				= LogManager.getLogger();

	protected long			minSampleTime	= 60000;
	protected Command		command			= null;
	protected Object		lastResult		= null;

	public Sampler(Command cmd)
	{
		this(cmd, 60000);
	}

	public Sampler(Command cmd, int minSampleTime)
	{
		this.command = cmd;
		this.minSampleTime = minSampleTime;
		this.setName("Sampler-" + cmd.getClass().getSimpleName());
	}

	@Override
	public void run()
	{
		L.info("Iniciando Sampler [{}]", this.command.getClass().getSimpleName());
		L.debug("Tiempo entre muestras: [{}] milisegundos", minSampleTime);

		this.setRunning();

		while (!this.isInterrupted())
		{
			long start_time = System.currentTimeMillis();

			try
			{
				lastResult = this.command.operate();
				if (Thread.interrupted()) continue; // Si se ha interrumpido la operacion, debemos abortar
				L.debug("Resultados obtenidos [{}]", lastResult);
				this.commitSuccess();
			}
			catch (HException e)
			{
				L.error("El Comando del Sampler lanzo una excepcion");
				L.catching(e);
			}

			long end_time = System.currentTimeMillis();
			long sleep_time = Math.max(minSampleTime - (end_time - start_time), 0);

			try
			{
				L.debug("Esperando [{}] milisegundos", sleep_time);
				Thread.sleep(sleep_time);
			}
			catch (InterruptedException e)
			{
				// Thread.interrupted();
			}
		}

		this.setStopped();

		L.info("Sampler [{}] detenido", this.command.getClass().getSimpleName());
	}

	@SuppressWarnings("unchecked")
	public <T> T getLastResult(Class<T> t) throws HException
	{
		if (this.lastResult != null)
		{
			if (t.getClass().isAssignableFrom(this.lastResult.getClass())) { return (T) this.lastResult; }
			throw new HException("No se pudo convertir la clase");
		}
		return null;
	}

	public Object getLastResult()
	{
		return this.lastResult;
	}

	@Override
	public long recurringInterval()
	{
		return this.minSampleTime;
	}

}
