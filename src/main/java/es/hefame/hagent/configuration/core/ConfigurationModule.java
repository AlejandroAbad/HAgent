package es.hefame.hagent.configuration.core;

import es.hefame.hcore.JsonEncodable;

public interface ConfigurationModule extends JsonEncodable
{
	public void configuration_changed();

}
