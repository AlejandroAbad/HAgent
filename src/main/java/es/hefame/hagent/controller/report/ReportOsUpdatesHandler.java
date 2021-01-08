package es.hefame.hagent.controller.report;

import java.io.IOException;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import es.hefame.hcore.JsonEncodable;
import es.hefame.hcore.HException;
import es.hefame.hagent.command.CommandFactory;
import es.hefame.hagent.command.os.updates.OsUpdatesCommand;
import es.hefame.hagent.command.os.updates.result.OsUpdatesResult;
import es.hefame.hagent.util.OperatingSystem;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.HttpConnection;

public class ReportOsUpdatesHandler extends HttpController
{

	//private static Logger L = LogManager.getLogger();

	@SuppressWarnings("unchecked")
	@Override
	public void get(HttpConnection t) throws IOException, HException
	{
		OsUpdatesCommand command = CommandFactory.new_command(OsUpdatesCommand.class);
		final OsUpdatesResult result = command.operate();

		JsonEncodable update_status = new JsonEncodable()
		{
			@Override
			public JSONAware jsonEncode()
			{
				JSONObject root = new JSONObject();
				root.put("os", OperatingSystem.get_os().jsonEncode());
				root.put("updates", result.jsonEncode());
				return root;
			}
		};

		t.response.send(update_status, 200);
	}

}
