package fr.doan.achilles.wrapper.builder;

import java.util.Iterator;
import java.util.Map.Entry;

import fr.doan.achilles.wrapper.EntryIteratorWrapper;

/**
 * EntryIteratorWrapperBuilder
 * 
 * @author DuyHai DOAN
 * 
 */
public class EntryIteratorWrapperBuilder<K, V> extends
		AbstractWrapperBuilder<EntryIteratorWrapperBuilder<K, V>, K, V>
{
	private Iterator<Entry<K, V>> target;

	public static <K, V> EntryIteratorWrapperBuilder<K, V> builder(Iterator<Entry<K, V>> target)
	{
		return new EntryIteratorWrapperBuilder<K, V>(target);
	}

	public EntryIteratorWrapperBuilder(Iterator<Entry<K, V>> target) {
		this.target = target;
	}

	public EntryIteratorWrapper<K, V> build()
	{
		EntryIteratorWrapper<K, V> iteratorWrapper = new EntryIteratorWrapper<K, V>(this.target);
		super.build(iteratorWrapper);
		return iteratorWrapper;
	}
}
