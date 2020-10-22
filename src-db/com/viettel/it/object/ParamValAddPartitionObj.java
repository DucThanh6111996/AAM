package com.viettel.it.object;

public class ParamValAddPartitionObj {
	private String tablespaceNameCheckSpace;
	private String tableOwnerCheckPartition;
	private String tableNameCheckPartition;
	private String partitionNameCheckpartition;
	private String tableOwnerAddPartition;
	private String tableNameAddPartition;
	private String partitionNameAddPartition;
	private String cycleAddPartition;
	private String tablespaceNameAddPartition;
	
	
	public ParamValAddPartitionObj(String tablespaceNameCheckSpace,
			String tableOwnerCheckPartition, String tableNameCheckPartition,
			String partitionNameCheckpartition, String tableOwnerAddPartition,
			String tableNameAddPartition, String partitionNameAddPartition,
			String cycleAddPartition, String tablespaceNameAddPartition) {
		super();
		this.tablespaceNameCheckSpace = tablespaceNameCheckSpace;
		this.tableOwnerCheckPartition = tableOwnerCheckPartition;
		this.tableNameCheckPartition = tableNameCheckPartition;
		this.partitionNameCheckpartition = partitionNameCheckpartition;
		this.tableOwnerAddPartition = tableOwnerAddPartition;
		this.tableNameAddPartition = tableNameAddPartition;
		this.partitionNameAddPartition = partitionNameAddPartition;
		this.cycleAddPartition = cycleAddPartition;
		this.tablespaceNameAddPartition = tablespaceNameAddPartition;
	}

	public String getTablespaceNameCheckSpace() {
		return tablespaceNameCheckSpace;
	}

	public void setTablespaceNameCheckSpace(String tablespaceNameCheckSpace) {
		this.tablespaceNameCheckSpace = tablespaceNameCheckSpace;
	}

	public String getTableOwnerCheckPartition() {
		return tableOwnerCheckPartition;
	}

	public void setTableOwnerCheckPartition(String tableOwnerCheckPartition) {
		this.tableOwnerCheckPartition = tableOwnerCheckPartition;
	}

	public String getTableNameCheckPartition() {
		return tableNameCheckPartition;
	}

	public void setTableNameCheckPartition(String tableNameCheckPartition) {
		this.tableNameCheckPartition = tableNameCheckPartition;
	}

	public String getPartitionNameCheckpartition() {
		return partitionNameCheckpartition;
	}

	public void setPartitionNameCheckpartition(
			String partitionNameCheckpartition) {
		this.partitionNameCheckpartition = partitionNameCheckpartition;
	}

	public String getTableOwnerAddPartition() {
		return tableOwnerAddPartition;
	}

	public void setTableOwnerAddPartition(String tableOwnerAddPartition) {
		this.tableOwnerAddPartition = tableOwnerAddPartition;
	}

	public String getTableNameAddPartition() {
		return tableNameAddPartition;
	}

	public void setTableNameAddPartition(String tableNameAddPartition) {
		this.tableNameAddPartition = tableNameAddPartition;
	}

	public String getPartitionNameAddPartition() {
		return partitionNameAddPartition;
	}

	public void setPartitionNameAddPartition(String partitionNameAddPartition) {
		this.partitionNameAddPartition = partitionNameAddPartition;
	}

	public String getCycleAddPartition() {
		return cycleAddPartition;
	}

	public void setCycleAddPartition(String cycleAddPartition) {
		this.cycleAddPartition = cycleAddPartition;
	}

	public String getTablespaceNameAddPartition() {
		return tablespaceNameAddPartition;
	}

	public void setTablespaceNameAddPartition(String tablespaceNameAddPartition) {
		this.tablespaceNameAddPartition = tablespaceNameAddPartition;
	}
	
	@Override
	public String toString() {
		return ("tablespaceNameCheckSpace:" + tablespaceNameCheckSpace
				+ "--tableOwnerCheckPartition:" + tableOwnerCheckPartition
				+ "--tableNameCheckPartition:" + tableNameCheckPartition
				+ "--partitionNameCheckpartition:" + partitionNameCheckpartition
				+ "--tableOwnerAddPartition:" + tableOwnerAddPartition
				+ "--tableNameAddPartition:" + tableNameAddPartition
				+ "--partitionNameAddPartition:" + partitionNameAddPartition
				+ "--cycleAddPartition:" + cycleAddPartition
				+ "--tablespaceNameAddPartition:" + tablespaceNameAddPartition);
	}

}
