package com.viettel.model;

// Created Oct 4, 2016 5:24:54 AM by quanns2

import com.viettel.bean.Service;
import com.viettel.exception.AppException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;

/**
 * @author quanns2
 */
@Entity
@Table(name = "ACTION_DT_FILE")
public class ActionDtFile implements java.io.Serializable {
	private static Logger logger = LogManager.getLogger(ActionDtFile.class);

	private Long id;
	private Long appGroupId;
	private String impactFile;
	private String rollbackFile;
	private String name;
	private String impactDescription;
	private String rollbackDescription;
	private String localFilename;
	private Integer impactTime;
	private String localRollbackFilename;

	public ActionDtFile() {
	}

	public ActionDtFile(Long id) {
		this.id = id;
	}

	public ActionDtFile(Long id, Long appGroupId, String impactFile, String rollbackFile, String name,
			String impactDescription, String rollbackDescription, String localFilename, Integer impactTime) {
		this.id = id;
		this.appGroupId = appGroupId;
		this.impactFile = impactFile;
		this.rollbackFile = rollbackFile;
		this.name = name;
		this.impactDescription = impactDescription;
		this.rollbackDescription = rollbackDescription;
		this.localFilename = localFilename;
		this.impactTime = impactTime;
	}

	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = 20, scale = 0)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ID")
	@SequenceGenerator(name = "ID", sequenceName = "ACTION_DT_FILE_SEQ", allocationSize=1)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "APP_GROUP_ID", precision = 20, scale = 0)
	public Long getAppGroupId() {
		return this.appGroupId;
	}

	public void setAppGroupId(Long appGroupId) {
		this.appGroupId = appGroupId;
	}

	@Column(name = "IMPACT_FILE", length = 400)
	public String getImpactFile() {
		return this.impactFile;
	}

	public void setImpactFile(String impactFile) {
		this.impactFile = impactFile;
	}

	@Column(name = "ROLLBACK_FILE", length = 400)
	public String getRollbackFile() {
		return this.rollbackFile;
	}

	public void setRollbackFile(String rollbackFile) {
		this.rollbackFile = rollbackFile;
	}

	@Column(name = "NAME", length = 200)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "IMPACT_DESCRIPTION", length = 4000)
	public String getImpactDescription() {
		return this.impactDescription;
	}

	public void setImpactDescription(String impactDescription) {
		this.impactDescription = impactDescription;
	}

	@Column(name = "ROLLBACK_DESCRIPTION", length = 4000)
	public String getRollbackDescription() {
		return this.rollbackDescription;
	}

	public void setRollbackDescription(String rollbackDescription) {
		this.rollbackDescription = rollbackDescription;
	}

	@Column(name = "LOCAL_FILENAME", length = 200)
	public String getLocalFilename() {
		return this.localFilename;
	}

	public void setLocalFilename(String localFilename) {
		this.localFilename = localFilename;
	}

	@Column(name = "IMPACT_TIME", precision = 10, scale = 0)
	public Integer getImpactTime() {
		return this.impactTime;
	}

	public void setImpactTime(Integer impactTime) {
		this.impactTime = impactTime;
	}

	@Column(name = "LOCAL_ROLLBACK_FILENAME")
	public String getLocalRollbackFilename() {
		return localRollbackFilename;
	}

	public void setLocalRollbackFilename(String localRollbackFilename) {
		this.localRollbackFilename = localRollbackFilename;
	}

	/*@Transient
	public Service getService() {
		IimClientService iimService = new IimClientServiceImpl();
		Service service = null;
		try {
			Action action = new ActionServiceImpl().findById(actionId);
			service = iimService.findServiceById(action.getNationCode(), appGroupId);
		} catch (AppException e) {
			logger.error(e.getMessage(), e);
		}

		return service;
	}*/
}
