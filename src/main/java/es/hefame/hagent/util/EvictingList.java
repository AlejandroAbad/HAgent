package es.hefame.hagent.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class EvictingList<E> implements Collection<E>
{

	private LinkedList<E>	inner;
	private int				limit;

	public EvictingList(int limit)
	{
		if (limit < 1) throw new IllegalArgumentException("El tamaño de la lista debe ser mayor que cero");
		inner = new LinkedList<E>();
		this.limit = limit;
	}

	@Override
	public synchronized boolean add(E o)
	{
		inner.add(o);
		while (inner.size() > limit)
		{
			inner.remove();
		}
		return true;
	}

	@Override
	public synchronized boolean addAll(Collection<? extends E> o)
	{
		inner.addAll(o);
		while (inner.size() > limit)
		{
			inner.remove();
		}
		return true;
	}

	@Override
	public synchronized void clear()
	{
		inner.clear();
	}

	@Override
	public synchronized boolean contains(Object o)
	{
		return inner.contains(o);
	}

	@Override
	public synchronized boolean containsAll(Collection<?> c)
	{
		return inner.containsAll(c);
	}

	@Override
	public synchronized boolean isEmpty()
	{
		return inner.isEmpty();
	}

	@Override
	public Iterator<E> iterator()
	{
		return inner.iterator();
	}

	@Override
	public synchronized boolean remove(Object o)
	{
		return inner.remove(o);
	}

	@Override
	public synchronized boolean removeAll(Collection<?> c)
	{
		return inner.removeAll(c);
	}

	@Override
	public synchronized boolean retainAll(Collection<?> c)
	{
		return inner.retainAll(c);
	}

	@Override
	public synchronized int size()
	{
		return inner.size();
	}

	@Override
	public synchronized Object[] toArray()
	{
		return inner.toArray();
	}

	@Override
	public synchronized <T> T[] toArray(T[] a)
	{
		return inner.toArray(a);
	}

}
