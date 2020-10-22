package com.viettel.it.object;

public class VendorNodeType {
	Long vendorId;
	Long nodeTypeId;
	private Long versionId;
	
	public VendorNodeType(Long vendorId, Long nodeTypeId) {
		super();
		this.vendorId = vendorId;
		this.nodeTypeId = nodeTypeId;
	}
	
	public VendorNodeType(Long vendorId, Long nodeTypeId, Long versionId) {
		super();
		this.vendorId = vendorId;
		this.nodeTypeId = nodeTypeId;
		this.versionId = versionId;
	}

	public Long getVendorId() {
		return vendorId;
	}
	public void setVendorId(Long vendorId) {
		this.vendorId = vendorId;
	}
	public Long getNodeTypeId() {
		return nodeTypeId;
	}
	public void setNodeTypeId(Long nodeTypeId) {
		this.nodeTypeId = nodeTypeId;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeTypeId == null) ? 0 : nodeTypeId.hashCode());
		result = prime * result + ((vendorId == null) ? 0 : vendorId.hashCode());
		result = prime * result + ((versionId == null) ? 0 : versionId.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VendorNodeType other = (VendorNodeType) obj;
		if (nodeTypeId == null) {
			if (other.nodeTypeId != null)
				return false;
		} else if (!nodeTypeId.equals(other.nodeTypeId))
			return false;
		if (vendorId == null) {
			if (other.vendorId != null)
				return false;
		} else if (!vendorId.equals(other.vendorId))
			return false;
		if (versionId == null) {
			if (other.versionId != null)
				return false;
		} else if (!versionId.equals(other.versionId))
			return false;
		return true;
	}
	public Long getVersionId() {
		return versionId;
	}
	public void setVersionId(Long versionId) {
		this.versionId = versionId;
	}
	
}
