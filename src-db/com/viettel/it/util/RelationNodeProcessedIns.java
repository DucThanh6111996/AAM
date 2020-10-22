package com.viettel.it.util;

import com.viettel.it.model.Node;
import com.viettel.it.model.RelationNode;

import java.util.concurrent.ConcurrentHashMap;

public class RelationNodeProcessedIns {

	private static RelationNodeProcessedIns instance;
	private ConcurrentHashMap<Long, RelationNode> mapNodeRelationProcessed = new ConcurrentHashMap<Long, RelationNode>();
	private ConcurrentHashMap<String, Node> mapNodeProcessed = new ConcurrentHashMap<String, Node>();
	
	public static RelationNodeProcessedIns getInstance() {
		if (instance == null) {
			synchronized (RelationNodeProcessedIns.class) {
				instance = new RelationNodeProcessedIns();
			}
		}
		return instance;
	}

	public ConcurrentHashMap<Long, RelationNode> getMapNodeRelationProcessed() {
		return mapNodeRelationProcessed;
	}

	public void setMapNodeRelationProcessed(
			ConcurrentHashMap<Long, RelationNode> mapNodeRelationProcessed) {
		this.mapNodeRelationProcessed = mapNodeRelationProcessed;
	}

	public ConcurrentHashMap<String, Node> getMapNodeProcessed() {
		return mapNodeProcessed;
	}

	public void setMapNodeProcessed(ConcurrentHashMap<String, Node> mapNodeProcessed) {
		this.mapNodeProcessed = mapNodeProcessed;
	}
	
}
