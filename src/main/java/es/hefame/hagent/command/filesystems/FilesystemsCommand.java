package es.hefame.hagent.command.filesystems;

import java.util.Map;

import es.hefame.hcore.HException;
import es.hefame.hagent.command.Command;
import es.hefame.hagent.command.filesystems.result.FilesystemResult;

public abstract class FilesystemsCommand implements Command
{
	public abstract Map<String, ? extends FilesystemResult> operate() throws HException;
}
