package com.viettel.persistence;

// Created May 5, 2016 4:56:37 PM by quanns2

import com.google.common.collect.Multimap;
import com.viettel.bean.Database;
import com.viettel.controller.Module;
import com.viettel.bean.Service;
import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.*;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Service interface for domain model class Action.
 * @see Action
 * @author quanns2
 */

public interface ActionService extends GenericDao<Action, Serializable> {
    //20190416_tudn_start import rule config
//    public void saveOrUpdateAction(Action action, List<ActionDetailApp> listDetailsApp, List<ActionDetailDatabase> detailDatabases, List<Module> dataTable, Multimap<Long, Long> checklistApp, List<Map.Entry<Long, Long>> kpiDbs, List<TestCase> testCases, List<ActionCustomGroup> customGroups,
//                                   Service[] selectedServices, Database[] selectedDatabases, List<ActionServer> actionServers, List<Module> ucttModules) throws AppException, SysException;
    public void saveOrUpdateAction(Action action, List<ActionDetailApp> listDetailsApp, List<ActionDetailDatabase> detailDatabases, List<Module> dataTable, Multimap<Long, Long> checklistApp, List<Map.Entry<Long, Long>> kpiDbs, List<TestCase> testCases, List<ActionCustomGroup> customGroups,
                                   Service[] selectedServices, Database[] selectedDatabases, List<ActionServer> actionServers, List<Module> ucttModules, List<RuleConfig> ruleConfigList) throws AppException, SysException;
    //20190416_tudn_end import rule config

    @Override
    public void delete(Action action) throws AppException, SysException;

    public void updateStatus(Long actionId, Integer status) throws AppException, SysException;

    public void updateIbmTicket(Long actionId, Integer ticket) throws AppException, SysException;

    public void updateVofficeStatus(Long actionId, String status) throws AppException, SysException;

    public List<Action> findByUser(String username) throws AppException, SysException;

    public Action findActionByCode(String code) throws AppException, SysException;

    public void updateCr(Long actionId, String crNumber, String crName, Date startTime, Date endTime, Long crState, String userExecute) throws AppException, SysException;

    public void updateCrNumber(Long actionId, String crNumber) throws AppException, SysException;

    public abstract void updateRollbackCr(Long paramLong, String paramString1, String paramString2, String paramString3, Date paramDate1, Date paramDate2)
            throws AppException, SysException;

    public void updateImpactUcttCr(Long actionId, String userApprove, String reason, String user, Date startTime, Date endTime)
            throws AppException, SysException;

    /*20181023_hoangnd_approval impact step_start*/
    public abstract void updateImpactStep(Long paramLong, String paramString1, String paramString2)
            throws AppException, SysException;
    /*20181023_hoangnd_approval impact step_end*/

    public List<Long> findListAppIds(Long actionId) throws AppException, SysException;

    public List<Action> findByTime(Date impactTime) throws AppException, SysException;

    public BigDecimal nextFileSeq() throws AppException, SysException;

    public List<Action> findList(int first, int pageSize, Map<String, Object> filters, Map<String, String> orders, List<Long> actionTypes) throws AppException, SysException;

    public int count(Map<String, Object> filters, List<Long> actionTypes) throws AppException, SysException;

    public Long updateVoResult(String signStatus, String documentCode, String lastSignEmail, String publishDate,
                               Long voTextId, String transCode) throws AppException, SysException;

    public List<Action> findCrUctt(Date currentDate, String username) throws AppException, SysException;

    public abstract List<Action> findCrUnclosed() throws AppException, SysException;

    public abstract void updateCrFromGnoc(Long paramLong1, Date paramDate1, Date paramDate2, Long paramLong2)
            throws AppException, SysException;

    public List<Action> findCrToExecute(DateTime currentDateTime, String username, Boolean isLoadTest) throws AppException, SysException;

    //20190826_tudn_start lap lich tac dong tu dong GNOC
//    public void updateAutoRunCr(Long actionId, Boolean autoRun) throws AppException, SysException;
    public void updateAutoRunCr(Long actionId, Boolean autoRun, Long typeConfirmGNOC, Long typeRunGNOC,String crLinkGNOC,Long cfStatusNocpro) throws AppException, SysException;
    //20190826_tudn_end lap lich tac dong tu dong GNOC

    public void updateRunning(Long actionId, Date actualStartTime, Integer runningStatus) throws AppException, SysException;

    public int countWaiting(Long actionId) throws AppException, SysException;

    public List<String> findIpReboot(Long actionId) throws AppException, SysException;

    public List<Action> findCrAuto(Date currentDate) throws AppException, SysException;

    /*20180813_hoangnd_send mail/sms action ci/cd_start*/
    public List<Action> findCiCdNextRun(Date currDate, String timeBeforeRun) throws AppException, SysException;

    /*20180912_hoangnd_dong bo work log_start*/
    public List<Action> findCiCdRun() throws AppException, SysException;
    /*20180912_hoangnd_dong bo work log_end*/

    public List<Action> findCiCdSuccess(Date currentDate) throws AppException, SysException;
	/*20180813_hoangnd_send mail/sms action ci/cd_end*/

    public List<Integer> findKbGroups(Long actionId) throws AppException, SysException;

    public List<Action> findCrByUser(String username, List<Long> crStates, Date startTime, Date endTime, Boolean includeAutoTicket) throws AppException, SysException;

    public abstract List<Action> findCrError() throws AppException, SysException;

    public abstract List<String> findModuleTypeNotDb() throws AppException, SysException;


    public void updateRunStatus(Long actionId, Integer status) throws AppException, SysException;
}
