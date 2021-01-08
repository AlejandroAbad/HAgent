package es.hefame.hagent.command.os.level.result;

import es.hefame.hcore.JsonEncodable;

public abstract class OsLevelResult implements JsonEncodable
{
	public abstract String get_version();

	public abstract int[] get_version_tokens();
}
