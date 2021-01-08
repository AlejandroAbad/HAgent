package es.hefame.hagent.controller;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import es.hefame.hcore.HException;
import es.hefame.hagent.command.Command;
import es.hefame.hagent.command.OsCommandExecutor;
import es.hefame.hagent.command.OsCommandResult;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;

public class QuickTestHandler extends HttpController
{
	private static Logger L = LogManager.getLogger();
	private static final Marker	TEST_MARKER	= MarkerManager.getMarker("TEST_MARKER");

	@Override
	public void get(HttpConnection t) throws IOException, HException
	{
		L.info("Quick Test");

		
		String subcmd = t.request.getURIField(1);
		if (subcmd != null && subcmd.equalsIgnoreCase("start")) {
			Command cmd = new Command() {
				
				@Override
				public Object operate() throws HException {
					try
					{
						OsCommandExecutor c = new OsCommandExecutor(TEST_MARKER, "chdev", "-l", "inet0", "-a", "route=host,-hopcount,0,,,,,,-static,10.85.245.135,172.30.10.57");
						return c.run();
					}
					catch (Exception e)
					{
						L.catching(e);
						return null;
					}
				}
			};
			Object r = cmd.operate();
			
			String txt = "NULL";
			if (r != null) {
				OsCommandResult res = (OsCommandResult) r;
				txt = res.get_exit_code() + "<br><hr><h1>stdin</h1><br>" +
				new String(res.get_stdout())  + "<br><hr><h1>stdin</h1><br>" +
				new String(res.get_stderr());		
			}
			
			t.response.send(txt, 200, "text/html");
			
		} else {
			Command cmd = new Command() {
				
				@Override
				public Object operate() throws HException {
					try
					{
						OsCommandExecutor c = new OsCommandExecutor(TEST_MARKER, "chdev", "-l", "inet0", "-a", "delroute=host,-hopcount,0,,,10.85.245.135,172.30.10.57");
						return c.run();
					}
					catch (Exception e)
					{
						L.catching(e);
						return null;
					}
				}
			};
			Object r = cmd.operate();
			
			String txt = "NULL";
			if (r != null) {
				OsCommandResult res = (OsCommandResult) r;
				txt = res.get_exit_code() + "<br><hr><h1>stdin</h1><br>" +
				new String(res.get_stdout())  + "<br><hr><h1>stdin</h1><br>" +
				new String(res.get_stderr());		
			}
			
			t.response.send(txt, 200, "text/html");
			
		}
		
		

		
		

		
		
	}

}
