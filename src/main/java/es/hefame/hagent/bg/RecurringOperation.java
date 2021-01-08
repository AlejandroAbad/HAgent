package es.hefame.hagent.bg;

public abstract class RecurringOperation extends Thread
{
	private boolean	running			= false;
	private long	last_success	= System.currentTimeMillis();

	public final void setRunning()
	{
		this.running = true;
	}

	public final void setStopped()
	{
		this.running = false;
	}

	public final boolean isRunning()
	{
		return this.running;
	}

	public final void commitSuccess()
	{
		last_success = System.currentTimeMillis();
	}

	public final long timeSinceLastSuccess()
	{
		return System.currentTimeMillis() - last_success;
	}

	public abstract long recurringInterval();

}
