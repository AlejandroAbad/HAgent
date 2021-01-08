package es.hefame.hagent.command;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hagent.util.OperatingSystem;
import es.hefame.hagent.util.exception.CommandNotSupportedException;

public class CommandFactory
{
	private static Logger L = LogManager.getLogger();

	public static <T extends Command> T new_command(Class<T> super_type, Object... args) throws CommandNotSupportedException
	{
		Class<T> resulting_class = get_os_dependent_class(super_type);
		L.trace("Instanciando clase [{}] en su subtipo [{}]", super_type.getName(), resulting_class.getName());
		try
		{
			Class<?>[] lo_constructor_paramenters = new Class<?>[args.length];

			for (int i = 0; i < args.length; i++)
			{
				lo_constructor_paramenters[i] = args[i].getClass();
				L.trace("El parametro [{}] del constructor es de tipo [{}]", i, args[i].getClass().getName());
			}

			Constructor<?> os_specific_class_constructor = resulting_class.getConstructor(lo_constructor_paramenters);

			@SuppressWarnings("unchecked")
			T operation = (T) os_specific_class_constructor.newInstance(args);

			return operation;
		}
		catch (InstantiationException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			L.catching(e);
			throw new CommandNotSupportedException(super_type);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends Command> Class<T> get_os_dependent_class(Class<T> super_type) throws CommandNotSupportedException
	{
		OperatingSystem OS = OperatingSystem.get_os();

		String os_independent_class_name = super_type.getName();
		String[] class_name_path = os_independent_class_name.split("\\.");
		class_name_path[class_name_path.length - 1] = OS.code + class_name_path[class_name_path.length - 1];

		String os_specific_class_name = class_name_path[0];
		for (int i = 1; i < class_name_path.length; i++)
		{
			os_specific_class_name += '.' + class_name_path[i];
		}

		Class<T> resulting_class;
		try
		{
			Class<?> casting_class = Class.forName(os_specific_class_name);
			if (Command.class.isAssignableFrom(casting_class))
			{
				resulting_class = (Class<T>) casting_class;
			}
			else
			{
				throw new ClassCastException(casting_class.getName() + " no es una subclase de " + Command.class.getName());
			}
		}
		catch (ClassNotFoundException e1)
		{
			try
			{
				Class<?> casting_class = Class.forName(os_independent_class_name);
				if (Command.class.isAssignableFrom(casting_class))
				{
					resulting_class = (Class<T>) casting_class;
				}
				else
				{
					throw new ClassCastException(casting_class.getName() + " no es una subclase de " + Command.class.getName());
				}
			}
			catch (ClassNotFoundException e)
			{
				L.catching(e);
				throw new CommandNotSupportedException(super_type);
			}
		}

		return resulting_class;
	}

}
