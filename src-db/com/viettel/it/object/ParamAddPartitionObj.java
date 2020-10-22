package com.viettel.it.object;

public class ParamAddPartitionObj {

	private Long serverId;
	private String tableName;
	private String tableOwner;
	private int partitionType;
	private String partitionPrefix;
	private int tablespaceType;
	private String tablespacePrefix;
	private int status;
	
	public ParamAddPartitionObj(Long serverId, String tableName,
			String tableOwner, int partitionType, String partitionPrefix,
			int tablespaceType, String tablespacePrefix, int status) {
		super();
		this.serverId = serverId;
		this.tableName = tableName;
		this.tableOwner = tableOwner;
		this.partitionType = partitionType;
		this.partitionPrefix = partitionPrefix;
		this.tablespaceType = tablespaceType;
		this.tablespacePrefix = tablespacePrefix;
		this.status = status;
	}

	public Long getServerId() {
		return serverId;
	}

	public void setServerId(Long serverId) {
		this.serverId = serverId;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableOwner() {
		return tableOwner;
	}

	public void setTableOwner(String tableOwner) {
		this.tableOwner = tableOwner;
	}

	public int getPartitionType() {
		return partitionType;
	}

	public void setPartitionType(int partitionType) {
		this.partitionType = partitionType;
	}

	public String getPartitionPrefix() {
		return partitionPrefix;
	}

	public void setPartitionPrefix(String partitionPrefix) {
		this.partitionPrefix = partitionPrefix;
	}

	public int getTablespaceType() {
		return tablespaceType;
	}

	public void setTablespaceType(int tablespaceType) {
		this.tablespaceType = tablespaceType;
	}

	public String getTablespacePrefix() {
		return tablespacePrefix;
	}

	public void setTablespacePrefix(String tablespacePrefix) {
		this.tablespacePrefix = tablespacePrefix;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
