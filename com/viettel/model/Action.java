package com.viettel.model;

// Created May 5, 2016 4:56:36 PM by quanns2

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author quanns2
 */
@Entity
@Table(name = "ACTION")
public class Action implements java.io.Serializable {

    private Long id;
    private String crNumber;
    private Long actionType;
    private Long kbGroup;
    private Long kbType;
    private String createdBy;
    private Date createdTime;
    private String reason;
    private Date beginTime;
    private String location;
    private Date endTime;
    private String person;
    private String sourceDir;
    private String crName;
    private String tdCode;
    private String updatedBy;
    private Date updatedTime;
    private Integer runStatus;
    private Date linkCrTime;
    private Long crState;
    private String staffCode;
    private Integer verifyStatus;

    private Integer ucttType;
    private String labelSign1;
    private String labelSign2;
    private String labelSign3;
    private String userSign1;
    private String userSign2;
    private String userSign3;

    private Date startRollback;
    private Date endRollback;
    private String userRollback;
    private String userGrant;

    private String signStatus;
    private String documentCode;
    private String lastSignEmail;
    private Long voTextId;
    private String publishDate;
    private Long adOrgId1;
    private Long adOrgId2;
    private Long adOrgId3;
    private String adOrgName1;
    private String adOrgName2;
    private String adOrgName3;
    private String fullName;

    private String crId;
    private Integer ucttState;

    private Integer ibmTicketId;

    private String approveUcttBy;
    private String approveRollbackBy;
    private String exeRollback;
    private Date startTimeRollback;
    private Date endTimeRollback;
    private String reasonRollback;
    private String exeImpactUctt;
    private Date startTimeImpactUctt;
    private Date endTimeImpactUctt;
    private String reasonImpactUctt;

    private Integer runAuto;
    private String userExecute;
    private Integer runningStatus;
    private Date actualStartTime;

    private ImpactProcess impactProcess;

    private Set<ActionItService> actionItServices = new HashSet<>();
    private Set<ActionDatabase> actionDatabases = new HashSet<>();
    private Set<ActionServer> actionServers = new HashSet<>();
    private Set<ActionModuleUctt> actionModuleUctts = new HashSet<>();

    private Integer includeTestbed;
    private Integer runTestbedStatus;
    private Boolean testbedMode;
    private Integer maxConcurrent;

    // Action reboot/shutdown (1:reboot, 2:shutdown)
    private Long actionRbSd;
    // Check is ignore stop application (Selected/unSelected)
    private Long ignoreStopApp;
    private Long runId;

    private Long ticketId;
    private Long templateId;

    /*20181023_hoangnd_continue fail step_start*/
    private String exeImpactStep;
    private String reasonImpactStep;
    private String workLogContent;

    private Integer mopType;
    private String service;

    private CatCountryBO catCountryBO;

    @ManyToOne()
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinColumn(name = "COUNTRY_CODE")
    public CatCountryBO getCatCountryBO() {
        return catCountryBO;
    }

    public void setCatCountryBO(CatCountryBO catCountryBO) {
        this.catCountryBO = catCountryBO;
    }

    @Column(name = "EXE_IMPACT_STEP", length = 200)
    public String getExeImpactStep() {
        return exeImpactStep;
    }

    public void setExeImpactStep(String exeImpactStep) {
        this.exeImpactStep = exeImpactStep;
    }

    @Column(name = "REASON_IMPACT_STEP", length = 200)
    public String getReasonImpactStep() {
        return reasonImpactStep;
    }

    public void setReasonImpactStep(String reasonImpactStep) {
        this.reasonImpactStep = reasonImpactStep;
    }
    /*20181023_hoangnd_continue fail step_end*/

    //20190826_tudn_start lap lich tac dong tu dong
    private Long typeConfirmGNOC;
    private Long typeRunGNOC;
    private String crLinkGNOC;
    private Long cfStatusNocpro;
    //20190826_tudn_end lap lich tac dong tu dong

    public Action() {
    }

    public Action(Long id) {
        this.id = id;
    }

    public Action(Long id, String crNumber, Long actionType, String createdBy, Date createdTime, String reason, Date beginTime, String location, Date endTime,
                  String person) {
        this.id = id;
        this.crNumber = crNumber;
        this.actionType = actionType;
        this.createdBy = createdBy;
        this.createdTime = createdTime;
        this.reason = reason;
        this.beginTime = beginTime;
        this.location = location;
        this.endTime = endTime;
        this.person = person;
    }

    @Id
    @Column(name = "ID", unique = true, nullable = false, precision = 22, scale = 0)
/*	@GeneratedValue(generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "ACTION_SEQ")*/
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ID")
    @SequenceGenerator(name = "ID", sequenceName = "ACTION_SEQ", allocationSize = 1)
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "CR_NUMBER", length = 200)
    public String getCrNumber() {
        return this.crNumber;
    }

    public void setCrNumber(String crNumber) {
        this.crNumber = crNumber;
    }

    @Column(name = "ACTION_TYPE", precision = 22, scale = 0)
    public Long getActionType() {
        return this.actionType;
    }

    public void setActionType(Long actionType) {
        this.actionType = actionType;
    }

    @Column(name = "CREATED_BY", length = 200)
    public String getCreatedBy() {
        return this.createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_TIME", length = 7)
    public Date getCreatedTime() {
        return this.createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    @Column(name = "REASON")
    public String getReason() {
        return this.reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "BEGIN_TIME", length = 7)
    public Date getBeginTime() {
        return this.beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    @Column(name = "LOCATION", length = 200)
    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "END_TIME")
    public Date getEndTime() {
        return this.endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Column(name = "PERSON", length = 50)
    public String getPerson() {
        return this.person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    @Column(name = "SOURCE_DIR")
    public String getSourceDir() {
        return sourceDir;
    }

    public void setSourceDir(String sourceDir) {
        this.sourceDir = sourceDir;
    }

    @Column(name = "CR_NAME")
    public String getCrName() {
        return crName;
    }

    public void setCrName(String crName) {
        this.crName = crName;
    }

    @Column(name = "TD_CODE")
    public String getTdCode() {
        return tdCode;
    }

    public void setTdCode(String tdCode) {
        this.tdCode = tdCode;
    }

    @Column(name = "UPDATED_BY")
    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATED_TIME", length = 7)
    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Column(name = "RUN_STATUS")
    public Integer getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(Integer runStatus) {
        this.runStatus = runStatus;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LINK_CR_TIME", length = 7)
    public Date getLinkCrTime() {
        return linkCrTime;
    }

    public void setLinkCrTime(Date linkCrTime) {
        this.linkCrTime = linkCrTime;
    }

    @Column(name = "CR_STATE")
    public Long getCrState() {
        return crState;
    }

    public void setCrState(Long crState) {
        this.crState = crState;
    }

    @Column(name = "STAFF_CODE")
    public String getStaffCode() {
        return staffCode;
    }

    public void setStaffCode(String staffCode) {
        this.staffCode = staffCode;
    }

    @Column(name = "VERIFY_STATUS")
    public Integer getVerifyStatus() {
        return verifyStatus;
    }

    public void setVerifyStatus(Integer verifyStatus) {
        this.verifyStatus = verifyStatus;
    }

    @Column(name = "UCTT_TYPE")
    public Integer getUcttType() {
        return ucttType;
    }

    public void setUcttType(Integer ucttType) {
        this.ucttType = ucttType;
    }

    @Column(name = "LABEL_SIGN_1")
    public String getLabelSign1() {
        return labelSign1;
    }

    public void setLabelSign1(String labelSign1) {
        this.labelSign1 = labelSign1;
    }

    @Column(name = "LABEL_SIGN_2")
    public String getLabelSign2() {
        return labelSign2;
    }

    public void setLabelSign2(String labelSign2) {
        this.labelSign2 = labelSign2;
    }

    @Column(name = "LABEL_SIGN_3")
    public String getLabelSign3() {
        return labelSign3;
    }

    public void setLabelSign3(String labelSign3) {
        this.labelSign3 = labelSign3;
    }

    @Column(name = "USER_SIGN_1")
    public String getUserSign1() {
        return userSign1;
    }

    public void setUserSign1(String userSign1) {
        this.userSign1 = userSign1;
    }

    @Column(name = "USER_SIGN_2")
    public String getUserSign2() {
        return userSign2;
    }

    public void setUserSign2(String userSign2) {
        this.userSign2 = userSign2;
    }

    @Column(name = "USER_SIGN_3")
    public String getUserSign3() {
        return userSign3;
    }

    public void setUserSign3(String userSign3) {
        this.userSign3 = userSign3;
    }

    @Column(name = "START_ROLLBACK")
    public Date getStartRollback() {
        return startRollback;
    }

    public void setStartRollback(Date startRollback) {
        this.startRollback = startRollback;
    }

    @Column(name = "END_ROLLBACK")
    public Date getEndRollback() {
        return endRollback;
    }

    public void setEndRollback(Date endRollback) {
        this.endRollback = endRollback;
    }

    @Column(name = "USER_ROLLBACK")
    public String getUserRollback() {
        return userRollback;
    }

    public void setUserRollback(String userRollback) {
        this.userRollback = userRollback;
    }

    @Column(name = "USER_GRANT")
    public String getUserGrant() {
        return userGrant;
    }

    public void setUserGrant(String userGrant) {
        this.userGrant = userGrant;
    }

    @Column(name = "SIGN_STATUS")
    public String getSignStatus() {
        return signStatus;
    }

    public void setSignStatus(String signStatus) {
        this.signStatus = signStatus;
    }

    @Column(name = "DOCUMENT_CODE")
    public String getDocumentCode() {
        return documentCode;
    }

    public void setDocumentCode(String documentCode) {
        this.documentCode = documentCode;
    }

    @Column(name = "LAST_SIGN_EMAIL")
    public String getLastSignEmail() {
        return lastSignEmail;
    }

    public void setLastSignEmail(String lastSignEmail) {
        this.lastSignEmail = lastSignEmail;
    }

    @Column(name = "VO_TEXT_ID")
    public Long getVoTextId() {
        return voTextId;
    }

    public void setVoTextId(Long voTextId) {
        this.voTextId = voTextId;
    }

    @Column(name = "PUBLISH_DATE")
    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    @Column(name = "AD_ORG_ID_1")
    public Long getAdOrgId1() {
        return adOrgId1;
    }

    public void setAdOrgId1(Long adOrgId1) {
        this.adOrgId1 = adOrgId1;
    }

    @Column(name = "AD_ORG_ID_2")
    public Long getAdOrgId2() {
        return adOrgId2;
    }

    public void setAdOrgId2(Long adOrgId2) {
        this.adOrgId2 = adOrgId2;
    }

    @Column(name = "AD_ORG_ID_3")
    public Long getAdOrgId3() {
        return adOrgId3;
    }

    public void setAdOrgId3(Long adOrgId3) {
        this.adOrgId3 = adOrgId3;
    }

    @Column(name = "AD_ORG_NAME_1")
    public String getAdOrgName1() {
        return adOrgName1;
    }

    public void setAdOrgName1(String adOrgName1) {
        this.adOrgName1 = adOrgName1;
    }

    @Column(name = "AD_ORG_NAME_2")
    public String getAdOrgName2() {
        return adOrgName2;
    }

    public void setAdOrgName2(String adOrgName2) {
        this.adOrgName2 = adOrgName2;
    }

    @Column(name = "AD_ORG_NAME_3")
    public String getAdOrgName3() {
        return adOrgName3;
    }

    public void setAdOrgName3(String adOrgName3) {
        this.adOrgName3 = adOrgName3;
    }

    @Column(name = "FULL_NAME")
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Column(name = "CR_ID")
    public String getCrId() {
        return crId;
    }

    public void setCrId(String crId) {
        this.crId = crId;
    }

    @Column(name = "UCTT_STATE")
    public Integer getUcttState() {
        return ucttState;
    }


    public void setUcttState(Integer ucttState) {
        this.ucttState = ucttState;
    }

    @Column(name = "APPROVE_UCTT_BY")
    public String getApproveUcttBy() {
        return this.approveUcttBy;
    }

    public void setApproveUcttBy(String approveUcttBy) {
        this.approveUcttBy = approveUcttBy;
    }

    @Column(name = "APPROVE_ROLLBACK_BY")
    public String getApproveRollbackBy() {
        return this.approveRollbackBy;
    }

    public void setApproveRollbackBy(String approveRollbackBy) {
        this.approveRollbackBy = approveRollbackBy;
    }

    @Column(name = "EXE_ROLLBACK")
    public String getExeRollback() {
        return this.exeRollback;
    }

    public void setExeRollback(String exeRollback) {
        this.exeRollback = exeRollback;
    }

    @Column(name = "START_TIME_ROLLBACK")
    public Date getStartTimeRollback() {
        return this.startTimeRollback;
    }

    public void setStartTimeRollback(Date startTimeRollback) {
        this.startTimeRollback = startTimeRollback;
    }

    @Column(name = "END_TIME_ROLLBACK")
    public Date getEndTimeRollback() {
        return this.endTimeRollback;
    }

    public void setEndTimeRollback(Date endTimeRollback) {
        this.endTimeRollback = endTimeRollback;
    }

    @Column(name = "REASON_ROLLBACK")
    public String getReasonRollback() {
        return this.reasonRollback;
    }

    public void setReasonRollback(String reasonRollback) {
        this.reasonRollback = reasonRollback;
    }

    @Column(name = "IBM_TICKET_ID")
    public Integer getIbmTicketId() {
        return ibmTicketId;
    }

    public void setIbmTicketId(Integer ibmTicketId) {
        this.ibmTicketId = ibmTicketId;
    }

    @Column(name = "KB_TYPE")
    public Long getKbType() {
        return kbType;
    }

    public void setKbType(Long kbType) {
        this.kbType = kbType;
    }

    @Column(name = "INCLUDE_TESTBED")
    public Integer getIncludeTestbed() {
        return includeTestbed;
    }

    public void setIncludeTestbed(Integer includeTestbed) {
        this.includeTestbed = includeTestbed;
    }

    @Column(name = "RUN_TESTBED_STATUS")
    public Integer getRunTestbedStatus() {
        return runTestbedStatus;
    }

    public void setRunTestbedStatus(Integer runTestbedStatus) {
        this.runTestbedStatus = runTestbedStatus;
    }

    @Transient
    public Long getKbGroup() {
        return kbGroup;
    }

    public void setKbGroup(Long kbGroup) {
        this.kbGroup = kbGroup;
    }

    @ManyToOne()
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinColumn(name = "PROCESS_ID")
    public ImpactProcess getImpactProcess() {
        return impactProcess;
    }

    public void setImpactProcess(ImpactProcess impactProcess) {
        this.impactProcess = impactProcess;
    }

    @OneToMany()
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinColumn(name = "ACTION_ID")
    public Set<ActionItService> getActionItServices() {
        return actionItServices;
    }

    public void setActionItServices(Set<ActionItService> actionItServices) {
        this.actionItServices = actionItServices;
    }

    @OneToMany()
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinColumn(name = "ACTION_ID")
    public Set<ActionDatabase> getActionDatabases() {
        return actionDatabases;
    }

    public void setActionDatabases(Set<ActionDatabase> actionDatabases) {
        this.actionDatabases = actionDatabases;
    }

    @OneToMany()
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinColumn(name = "ACTION_ID")
    public Set<ActionServer> getActionServers() {
        return actionServers;
    }

    public void setActionServers(Set<ActionServer> actionServers) {
        this.actionServers = actionServers;
    }

    @OneToMany()
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinColumn(name = "ACTION_ID")
    public Set<ActionModuleUctt> getActionModuleUctts() {
        return actionModuleUctts;
    }

    public void setActionModuleUctts(Set<ActionModuleUctt> actionModuleUctts) {
        this.actionModuleUctts = actionModuleUctts;
    }

    @Column(name = "RUN_AUTO")
    public Integer getRunAuto() {
        return runAuto;
    }

    public void setRunAuto(Integer runAuto) {
        this.runAuto = runAuto;
    }

    @Column(name = "USER_EXECUTE")
    public String getUserExecute() {
        return userExecute;
    }

    public void setUserExecute(String userExecute) {
        this.userExecute = userExecute;
    }

    @Column(name = "RUNNING_STATUS")
    public Integer getRunningStatus() {
        return runningStatus;
    }

    public void setRunningStatus(Integer runningStatus) {
        this.runningStatus = runningStatus;
    }

    @Column(name = "ACTUAL_START_TIME")
    public Date getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(Date actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    @Transient
    public Boolean getTestbedMode() {
        return testbedMode;
    }

    public void setTestbedMode(Boolean testbedMode) {
        this.testbedMode = testbedMode;
    }

    @Column(name = "MAX_CONCURRENT")
    public Integer getMaxConcurrent() {
        return maxConcurrent;
    }

    public void setMaxConcurrent(Integer maxConcurrent) {
        this.maxConcurrent = maxConcurrent;
    }

    @Column(name = "ACTION_RB_SD")
    public Long getActionRbSd() {
        return actionRbSd;
    }

    public void setActionRbSd(Long actionRbSd) {
        this.actionRbSd = actionRbSd;
        if (this.actionRbSd == null) {
            this.actionRbSd = 1L;
        }
    }

    @Column(name = "IGNORE_STOP_APP")
    public Long getIgnoreStopApp() {
        return ignoreStopApp;
    }

    public void setIgnoreStopApp(Long ignoreStopApp) {
        this.ignoreStopApp = ignoreStopApp;
    }

    @Column(name = "TICKET_ID")
    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    @Column(name = "TEMPLATE_ID")
    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    @Column(name = "EXE_IMPACT_UCTT")
    public String getExeImpactUctt() {
        return exeImpactUctt;
    }

    public void setExeImpactUctt(String exeImpactUctt) {
        this.exeImpactUctt = exeImpactUctt;
    }

    @Column(name = "START_TIME_IMPACT_UCTT")
    public Date getStartTimeImpactUctt() {
        return startTimeImpactUctt;
    }

    public void setStartTimeImpactUctt(Date startTimeImpactUctt) {
        this.startTimeImpactUctt = startTimeImpactUctt;
    }

    @Column(name = "END_TIME_IMPACT_UCTT")
    public Date getEndTimeImpactUctt() {
        return endTimeImpactUctt;
    }

    public void setEndTimeImpactUctt(Date endTimeImpactUctt) {
        this.endTimeImpactUctt = endTimeImpactUctt;
    }

    @Column(name = "REASON_IMPACT_UCTT")
    public String getReasonImpactUctt() {
        return reasonImpactUctt;
    }

    public void setReasonImpactUctt(String reasonImpactUctt) {
        this.reasonImpactUctt = reasonImpactUctt;
    }

    @Transient
//    @Column(name = "WORK_LOG_CONTENT")
    public String getWorkLogContent() {
        return workLogContent;
    }

    public void setWorkLogContent(String workLogContent) {
        this.workLogContent = workLogContent;
    }

    //20190826_tudn_start lap lich tac dong tu dong GNOC
    @Column(name = "TYPE_CONFIRM_GNOC")
    public Long getTypeConfirmGNOC() {
        return typeConfirmGNOC;
    }

    public void setTypeConfirmGNOC(Long typeConfirmGNOC) {
        this.typeConfirmGNOC = typeConfirmGNOC;
    }

    @Column(name = "TYPE_RUN_GNOC")
    public Long getTypeRunGNOC() {
        return typeRunGNOC;
    }

    public void setTypeRunGNOC(Long typeRunGNOC) {
        this.typeRunGNOC = typeRunGNOC;
    }

    @Column(name = "CR_LINK_GNOC")
    public String getCrLinkGNOC() {
        return crLinkGNOC;
    }

    public void setCrLinkGNOC(String crLinkGNOC) {
        this.crLinkGNOC = crLinkGNOC;
    }

    @Column(name = "CF_STATUS_NOCPRO")
    public Long getCfStatusNocpro() {
        return cfStatusNocpro;
    }

    public void setCfStatusNocpro(Long cfStatusNocpro) {
        this.cfStatusNocpro = cfStatusNocpro;
    }
    //20190826_tudn_end lap lich tac dong tu dong GNOC

    //ThanhTD
    @Column(name = "MOP_TYPE")
    public Integer getMopType() {
        return mopType;
    }

    public void setMopType(Integer mopType) {
        this.mopType = mopType;
    }

    @Column(name = "SERVICE")
    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
    //End ThanhTD


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Action action = (Action) o;

        return new EqualsBuilder()
                .append(id, action.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("tdCode", tdCode)
                .toString();
    }

    public String actionLog() {
        return new ToStringBuilder(this)
                .append("tdCode", tdCode)
                .append("crNumber", crNumber)
                .append("crName", crName)
                .toString();
    }

    @Transient
    public Long getRunId() {
        return runId;
    }

    public void setRunId(Long runId) {
        this.runId = runId;
    }
}
