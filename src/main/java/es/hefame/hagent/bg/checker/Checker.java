package es.hefame.hagent.bg.checker;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.hagent.bg.RecurringOperation;
import es.hefame.hagent.configuration.CONF;
import es.hefame.hagent.util.agent.AgentInfo;
import es.hefame.hagent.util.mail.html.HtmlAnchor;
import es.hefame.hagent.util.mail.html.HtmlList;

public abstract class Checker extends RecurringOperation {
	private static Logger L = LogManager.getLogger();
	protected long minCheckTime = 60000;
	protected long delayIfException = 0;
	private Date noAlertsBefore = null;
	private String name = null;

	public Checker(String name, int minCheckTime) {
		this.name = name;
		this.minCheckTime = minCheckTime;

		// Thread.setName
		this.setName("Checker-" + this.name);
	}

	public Checker(String name) {
		this(name, 60000);
	}

	@Override
	public void run() {
		L.info("Iniciando Checker [{}][{}]", name, this.getClass().getSimpleName());
		L.debug("Tiempo entre checkeos: [{}ms]", minCheckTime);

		this.setRunning();
		boolean exceptionThrowed = false;

		while (!this.isInterrupted() && this.isRunning()) {
			long startTime = System.currentTimeMillis();
			exceptionThrowed = false;

			try {
				this.operate();
				if (Thread.interrupted()) {
					continue; // TODO: Si se ha interrumpido la operacion, debemos abortar
				}
				this.commitSuccess();
			} catch (HException e) {
				L.error("El Checker [{}] lanzo una excepcion", name);
				L.catching(e);
				exceptionThrowed = true;
			}

			long end_time = System.currentTimeMillis();
			long sleep_time = Math.max(minCheckTime - (end_time - startTime), 0);

			if (exceptionThrowed && this.delayIfException > 0) {
				sleep_time = Math.max(sleep_time, this.delayIfException);
			}

			if (sleep_time > 0) {
				try {
					L.debug("Esperando [{}] milisegundos", sleep_time);
					Thread.sleep(sleep_time);
				} catch (InterruptedException e) {
				}
			}

		}

		this.setStopped();

		L.info("Checker [{}] detenido", name);

	}

	public abstract void operate() throws HException;

	public Date stop_alerting(int secs) {
		long now = System.currentTimeMillis();
		noAlertsBefore = new Date(now + (secs * 1000));
		L.info("Alertas detenidas para el Checker [{}] hasta el [{}]", this.name, noAlertsBefore);
		return noAlertsBefore;
	}

	public void resumeAlerting() {
		noAlertsBefore = null;
		L.info("Alertas reiniciadas para el Checker [{}]", this.name);
	}

	public boolean alertsEnabled() {
		if (noAlertsBefore == null) {
			return true;
		}
		if (noAlertsBefore.before(new Date())) {
			noAlertsBefore = null;
			return true;
		} else {
			return false;
		}
	}

	public String getAlertLinks() {
		HtmlList list = new HtmlList();
		list.add_item(new HtmlAnchor("6 horas", "http://" + AgentInfo.get_fqdn_hostname() + ":" + CONF.agent.port
				+ "/alarms/" + this.name + "/stop/" + (6 * 60)));
		list.add_item(new HtmlAnchor("12 horas", "http://" + AgentInfo.get_fqdn_hostname() + ":" + CONF.agent.port
				+ "/alarms/" + this.name + "/stop/" + (6 * 60)));
		list.add_item(new HtmlAnchor("1 d&iacute;a", "http://" + AgentInfo.get_fqdn_hostname() + ":" + CONF.agent.port
				+ "/alarms/" + this.name + "/stop/" + (24 * 60)));
		list.add_item(new HtmlAnchor("2 d&iacute;as", "http://" + AgentInfo.get_fqdn_hostname() + ":" + CONF.agent.port
				+ "/alarms/" + this.name + "/stop/" + (2 * 24 * 60)));
		list.add_item(new HtmlAnchor("1 semana", "http://" + AgentInfo.get_fqdn_hostname() + ":" + CONF.agent.port
				+ "/alarms/" + this.name + "/stop/" + (7 * 24 * 60)));
		return "<p>Puede detener temporalemente estas alertas con los siguientes enlaces:<p>" + list.toString();
	}

	public String getCheckerName() {
		return this.name;
	}

	@Override
	public long recurringInterval() {
		return this.minCheckTime;
	}

}
