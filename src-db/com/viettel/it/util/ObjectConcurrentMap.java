/*
 * Created on Oct 31, 2013
 *
 * Copyright (C) 2013 by Viettel Network Company. All rights reserved
 */
package com.viettel.it.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ObjectConcurrentMap.java
 *
 * @author Nguyen Hai Ha<hanh45@viettel.com.vn>
 * @since Oct 31, 2013
 * @version 1.0.0
 */
public class ObjectConcurrentMap {
	private static ObjectConcurrentMap instance = null;
	
	// Chua thong tin ve cac thread dang chay cua 1 dia chi IP hoac username
	private ConcurrentMap<String, ConcurrentMap<?,?>> mapObject = new ConcurrentHashMap<>();
	
	private ObjectConcurrentMap(){}
	
	// Khoi tao singleton
	public static ObjectConcurrentMap getInstance(){
		if (instance == null) {
			synchronized (ObjectConcurrentMap.class) {
				instance = new ObjectConcurrentMap();
			}
		}
		return instance;
	}

	/**
	 * Put gia tri toi map
	 */
	public void put(String key, ConcurrentMap<?,?> value){
		getMapObject().putIfAbsent(key, value);
	}
	
	/**
	 * Lay gia tri tu map
	 */
	public ConcurrentMap<?,?> get(String key){
		return getMapObject().get(key);
	}
	
	/**
	 * Remove gia tri tu map.
	 */
	public void remove(String key){
		getMapObject().remove(key);
	}
	
	/**
	 * @return the mapObject
	 */
	public ConcurrentMap<String, ConcurrentMap<?,?>> getMapObject() {
		return mapObject;
	}
}
