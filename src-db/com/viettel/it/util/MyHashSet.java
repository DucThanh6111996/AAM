package com.viettel.it.util;

import java.util.HashSet;
import java.util.Iterator;

@SuppressWarnings("serial")
public class MyHashSet<E> extends HashSet<E> {
	@Override
	public String toString() {
		String result = "";
		for (Iterator<E> iterator = this.iterator(); iterator.hasNext();) {
			E obj = iterator.next();
			result += obj + ",";
		}
        return result.replaceAll(",$", "");
	}

	@Override
	public boolean add(E e) {
		// TODO Auto-generated method stub
//		System.err.println();
		return super.add(e);
	}
	
}
