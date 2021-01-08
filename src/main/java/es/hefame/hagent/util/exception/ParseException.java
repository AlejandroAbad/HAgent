package es.hefame.hagent.util.exception;

import es.hefame.hcore.http.HttpException;

public class ParseException extends HttpException
{
	private static final long serialVersionUID = 7632548169520933149L;

	public ParseException(String message, Throwable cause)
	{
		super(400, message, cause);
	}

	public ParseException(String message)
	{
		super(400, message);
	}

}
