package com.viettel.persistence;

// Created May 5, 2016 4:56:37 PM by quanns2


import com.google.common.collect.Multimap;
import com.viettel.bean.Database;
import com.viettel.controller.ActionDetailApp;
import com.viettel.controller.Module;
import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.*;
import com.viettel.controller.AamConstants;
import com.viettel.util.HibernateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.criterion.*;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
import org.joda.time.DateTime;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.jms.Session;
import javax.transaction.Transaction;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
 * Service implement for interface ActionService.
 *
 * @author quanns2
 * @see ActionService
 */

@Service(value = "actionService")
@Scope("session")
public class ActionServiceImpl extends GenericDaoImpl<Action, Serializable> implements ActionService, Serializable {
    private static Logger logger = LogManager.getLogger(ActionServiceImpl.class);

    @Override
    public void saveOrUpdateAction(Action action, List<ActionDetailApp> listDetailsApp, List<ActionDetailDatabase> detailDatabases, List<Module> dataTable, Multimap<Long, Long> checklistApp, List<Map.Entry<Long, Long>> kpiDbs, List<TestCase> testCases, List<ActionCustomGroup> customGroups,
                                   com.viettel.bean.Service[] selectedServices, Database[] selectedDatabases, List<ActionServer> actionServers, List<Module> ucttModules, List<RuleConfig> ruleConfigList) throws AppException, SysException {
        Session session = HibernateUtil.openSession();
        Transaction tx = null;
        /*20180904_hoangnd_fix update wo_start*/
        boolean isEdit = false;
        /*20180904_hoangnd_fix update wo_end*/
        try {
            tx = session.beginTransaction();

            if (action.getId() != null) {

                /*20180904_hoangnd_fix update wo_start*/
                isEdit = true;
                /*20180904_hoangnd_fix update wo_end*/


                String sql = "delete from ACTION_MODULE_CHECKLIST where ACTION_MODULE_ID in (select id from ACTION_MODULE where ACTION_ID=:actionId)";
                SQLQuery query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();

                sql = "delete from ACTION_DB_CHECKLIST where ACTION_ID=:actionId";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();

                sql = "delete from ACTION_MODULE where ACTION_ID=:actionId";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();

                sql = "delete from TEST_CASE where ACTION_ID=:actionId";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();

                //20190416_tudn_start import rule config
                sql = "delete from ACTION_DETAIL_APP_RULE_CONFIG where ACTION_ID=:actionId";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();
                //20190416_tudn_start import rule config

                sql = "delete from ACTION_DETAIL_APP where ACTION_ID=:actionId";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();

                sql = "delete from ACTION_DETAIL_DATABASE where ACTION_ID=:actionId";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();

                sql = "delete from action_custom_action where GROUP_ID in (select id from action_custom_group where ACTION_ID=:actionId)";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();

                sql = "delete from action_custom_group where ACTION_ID=:actionId";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();

                sql = "delete from ACTION_MODULE_UCTT where ACTION_ID=:actionId";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();

                sql = "delete from ACTION_SERVER where ACTION_ID=:actionId";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();

                sql = "delete from ACTION_SERVICE where ACTION_ID=:actionId";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();

                sql = "delete from ACTION_DATABASE where ACTION_ID=:actionId";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();
            }

            if (action.getId() == null) {
                String sql = "SELECT TD_CODE_SEQ.nextval FROM DUAL";
                SQLQuery query = session.createSQLQuery(sql);
                BigDecimal seq = (BigDecimal) query.uniqueResult();

                if (action.getActionType().equals(AamConstants.ACTION_TYPE.ACTION_TYPE_KB_UCTT))
                    action.setTdCode("KB_UCTT_" + seq);
                else
                    action.setTdCode("TD_CNTT_" + seq);
            }

            action.setRunTestbedStatus(0);
            action.setIncludeTestbed(0);
            for (Module module : dataTable) {
                if (module.getTestbedMode() != null && module.getTestbedMode() == AamConstants.TESTBED_MODE.TESTBED)
                    action.setIncludeTestbed(1);
            }
            session.saveOrUpdate(action);

            Map<ActionModule, Long> actionModuleIds = new HashMap<>();
            Map<Long, Module> modules = new HashMap<>();
            if (dataTable != null && !dataTable.isEmpty()) {
                for (Module module : dataTable) {
                    ActionModule actionModule = new ActionModule();
                    actionModule.setActionId(action.getId());
                    actionModule.setModuleId(module.getModuleId());
                    actionModule.setFunctionCode(module.getFunctionCode());

                    actionModule.setActionType(module.getActionType());
                    actionModule.setKbGroup(module.getKbGroup());
                    actionModule.setTestbedMode(module.getTestbedMode());

                    actionModule.setIpServer(module.getIpServer());
                    actionModule.setStartService(module.getStartService());
                    actionModule.setStopService(module.getStopService());
                    actionModule.setRestartService(module.getRestartService());
                    actionModule.setViewStatus(module.getViewStatus());
                    actionModule.setDeleteCache(module.getDeleteCache());
                    actionModule.setInstalledUser(module.getUsername());
                    actionModule.setPath(module.getExecutePath());
                    actionModule.setAppCode(module.getModuleCode());
                    actionModule.setAppTypeCode(module.getModuleTypeCode());
                    actionModule.setOsType(module.getOsType());
                    actionModule.setOsName(module.getOsName());
                    actionModule.setModuleName(module.getModuleName());
                    actionModule.setServiceName(module.getServiceName());
                    actionModule.setGroupModuleCode(module.getGroupModuleCode());
                    actionModule.setGroupModuleName(module.getGroupModuleName());

                    actionModule.setLogLink(module.getFullLogStartPath());
                    actionModule.setKeyword(module.getStartSuccessKey());
                    actionModule.setKeyStatusStart(module.getStatusSuccessKey());

                    actionModule.setRestartSuccessKey(module.getRestartSuccessKey());
                    actionModule.setStopSuccessKey(module.getStopSuccessKey());
                    actionModule.setDeleteCacheSuccessKey(module.getDeleteCacheSuccessKey());

                    actionModule.setStartFailKey(module.getStartFailKey());
                    actionModule.setStatusFailKey(module.getStatusFailKey());
                    actionModule.setRestartFailKey(module.getRestartFailKey());
                    actionModule.setStopFailKey(module.getStopFailKey());
                    actionModule.setDeleteCacheFailKey(module.getDeleteCacheFailKey());

                    actionModule.setStartOutput(module.getStartOutput());
                    actionModule.setStatusOutput(module.getStatusOutput());
                    actionModule.setRestartOutput(module.getRestartOutput());
                    actionModule.setStopOutput(module.getStopOutput());
                    actionModule.setDeleteCacheOutput(module.getDeleteCacheOutput());

//                    actionModule.set(module.getStatusSuccessKey());

                    /*IimService iimService = new IimServiceImpl();

                    LogOs logOs = iimService.findLogByModule(module.getModuleId(), AamConstants.KEY_LOG_START);
                    if (logOs != null) {
                        actionModule.setLogLink(logOs.getFullPath());
                        actionModule.setKeyword(logOs.getKeyWord());
                    }

                    logOs = iimService.findLogByModule(module.getModuleId(), AamConstants.KEY_STATUS_START);
                    if (logOs != null) {
                        actionModule.setKeyStatusStart(logOs.getKeyWord());
                    }*/

                    session.saveOrUpdate(actionModule);
                    actionModuleIds.put(actionModule, actionModule.getId());
                    modules.put(module.getModuleId(), module);
                }
            }

            //20190416_tudn_start import rule config
            Map<String, Long> actionDetailAppIds = new HashMap<>();
            //20190416_tudn_end import rule config
            if (listDetailsApp != null)
                for (ActionDetailApp actionDetailApp : listDetailsApp) {
                    actionDetailApp.setId(null);
                    actionDetailApp.setActionId(action.getId());

//                    logger.info(actionDetailApp);
                    session.save(actionDetailApp);
//                    logger.info(actionDetailApp);
                    //20190416_tudn_start import rule config
                    if (AamConstants.RUN_STEP.STEP_UPCODE.equals(actionDetailApp.getGroupAction()) || AamConstants.RUN_STEP.STEP_UPCODE_STOP_START.equals(actionDetailApp.getGroupAction())) {
                        actionDetailAppIds.put(modules.get(actionDetailApp.getModuleId()).getModuleCode() + "#" + actionDetailApp.getUpcodePath(), actionDetailApp.getId());
                    }
                    //20190416_tudn_end import rule config
                }

            //20190416_tudn_start import rule config
            if (ruleConfigList != null)
                for (RuleConfig ruleConfig : ruleConfigList) {
                    if (actionDetailAppIds.containsKey(ruleConfig.getModuleCode() + "#" + ruleConfig.getPathFile())) {
                        ruleConfig.setActionDetailAppId(actionDetailAppIds.get(ruleConfig.getModuleCode() + "#" + ruleConfig.getPathFile()));
                        ruleConfig.setActionId(action.getId());
                        session.save(ruleConfig);
                    }
                }
            //20190416_tudn_end import rule config

            if (detailDatabases != null)
                for (ActionDetailDatabase detailDatabase : detailDatabases) {
                    detailDatabase.setId(null);
                    detailDatabase.setActionId(action.getId());
                    session.saveOrUpdate(detailDatabase);
                }

            for (Map.Entry<Long, Collection<Long>> collEntry : checklistApp.asMap().entrySet()) {
                Collection<Long> checkListIds = collEntry.getValue();
                for (Long checkListId : checkListIds) {
                    ActionModule actionModule = new ActionModule();
                    actionModule.setActionId(action.getId());
                    actionModule.setModuleId(collEntry.getKey());

                    ActionModuleChecklist actionModuleChecklist = new ActionModuleChecklist();
                    actionModuleChecklist.setActionModuleId(actionModuleIds.get(actionModule));
                    actionModuleChecklist.setChecklistId(checkListId);

                    session.saveOrUpdate(actionModuleChecklist);
                }
            }

            for (Map.Entry<Long, Long> kpiDb : kpiDbs) {
                ActionDbChecklist dbChecklist = new ActionDbChecklist();
                dbChecklist.setActionId(action.getId());
                dbChecklist.setAppDbId(kpiDb.getKey());
                dbChecklist.setChecklistId(kpiDb.getValue());

                session.saveOrUpdate(dbChecklist);
            }

            if (testCases != null)
                for (TestCase testCase : testCases) {
                    testCase.setId(null);
                    testCase.setActionId(action.getId());
                    session.saveOrUpdate(testCase);
                }

            if (customGroups != null) {

                for (ActionCustomGroup customGroup : customGroups) {
                    customGroup.setActionId(action.getId());
                    if (customGroup.getKbGroup() == null)
                        customGroup.setKbGroup(1);
                    session.save(customGroup);
                    Set<ActionCustomAction> customActions = customGroup.getActionCustomActions();
                    for (ActionCustomAction customAction : customActions) {
                        customAction.setActionCustomGroup(customGroup);
                        session.save(customAction);
                    }
                }
            }

            if (selectedServices != null) {
                for (com.viettel.bean.Service service : selectedServices) {
                    ActionItService actionItService = new ActionItService();
                    actionItService.setId(null);
                    actionItService.setActionId(action.getId());
                    actionItService.setServiceId(service.getServiceId());
                    session.saveOrUpdate(actionItService);
                }
            }

            if (selectedDatabases != null) {
                for (Database database : selectedDatabases) {
                    ActionDatabase actionDatabase = new ActionDatabase();
                    actionDatabase.setId(null);
                    actionDatabase.setActionId(action.getId());
                    actionDatabase.setDbId(database.getDbId());
                    session.saveOrUpdate(actionDatabase);
                }
            }

            if (actionServers != null) {
                for (ActionServer actionServer : actionServers) {
//                    ActionServer actionServer = new ActionServer();
                    actionServer.setId(null);
                    actionServer.setActionId(action.getId());
//                    actionServer.setIpServer(ipServer);
                    session.saveOrUpdate(actionServer);
                }
            }

            if (ucttModules != null) {
                for (Module module : ucttModules) {
                    ActionModuleUctt actionModule = new ActionModuleUctt();
                    actionModule.setId(null);
                    actionModule.setActionId(action.getId());
                    actionModule.setModuleId(module.getModuleId());
                    session.saveOrUpdate(actionModule);
                }
            }

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public void delete(Action action) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            if (action.getId() != null) {
                String sql = "delete from ACTION_MODULE_CHECKLIST where ACTION_MODULE_ID in (select id from ACTION_MODULE where ACTION_ID=:actionId)";
                SQLQuery query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();

                sql = "delete from ACTION_DB_CHECKLIST where ACTION_ID=:actionId";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();

                sql = "delete from ACTION_MODULE where ACTION_ID=:actionId";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();

                sql = "delete from TEST_CASE where ACTION_ID=:actionId";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();

                sql = "delete from ACTION_DETAIL_APP where ACTION_ID=:actionId";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();

                sql = "delete from ACTION_DETAIL_DATABASE where ACTION_ID=:actionId";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();

                sql = "delete from ACTION_MODULE_UCTT where ACTION_ID=:actionId";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();

                sql = "delete from ACTION_SERVER where ACTION_ID=:actionId";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();

                sql = "delete from ACTION_SERVICE where ACTION_ID=:actionId";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();

                sql = "delete from ACTION_DATABASE where ACTION_ID=:actionId";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();

                sql = "delete from ACTION_HISTORY where ACTION_ID=:actionId";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", action.getId());
                query.executeUpdate();
            }

            session.delete(action);

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public void updateStatus(Long actionId, Integer status) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            if (actionId != null) {
                String sql = "update ACTION SET RUN_STATUS=:runStatus where ID=:actionId";
                SQLQuery query = session.createSQLQuery(sql);
                query.setParameter("actionId", actionId);
                query.setParameter("runStatus", status);
                query.executeUpdate();

                sql = "update ACTION_DETAIL_APP set RUN_STATUS=null, BACKUP_STATUS=null where ACTION_ID=:actionId";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", actionId);
                query.executeUpdate();

                sql = "update ACTION_DETAIL_DATABASE set RUN_STATUS=null, BACKUP_STATUS=null where ACTION_ID=:actionId";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", actionId);
                query.executeUpdate();

                sql = "update (select a.run_status, a.rollback_test_status from action_custom_group g join action_custom_action a on g.id=a.group_id  where g.action_id=:actionId) t set t.run_status=null, t.rollback_test_status=null";
                query = session.createSQLQuery(sql);
                query.setParameter("actionId", actionId);
                query.executeUpdate();
            }

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public void updateIbmTicket(Long actionId, Integer ticket) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            if (actionId != null) {
                String sql = "update ACTION SET IBM_TICKET_ID=:ticket where ID=:actionId";
                SQLQuery query = session.createSQLQuery(sql);
                query.setParameter("actionId", actionId);
                query.setParameter("ticket", ticket);
                query.executeUpdate();

            }

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public void updateVofficeStatus(Long actionId, String status) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            if (actionId != null) {
                String sql = "update ACTION SET sign_Status=:signStatus where ID=:actionId";
                SQLQuery query = session.createSQLQuery(sql);
                query.setParameter("actionId", actionId);
                query.setParameter("signStatus", status);
                query.executeUpdate();
            }

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<Action> findByUser(String username) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        List<Action> objects = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(Action.class);
            criteria.add(Restrictions.eq("createdBy", username));
            criteria.add(Restrictions.eq("actionType", AamConstants.ACTION_TYPE.ACTION_TYPE_CR_NORMAL));
            criteria.add(Restrictions.eq("crNumber", "CR_CNTT_TEMP_999999"));
            criteria.addOrder(Order.desc("createdTime"));

            objects = criteria.list();
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);

        } catch (Exception e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);

        } finally {
            if (session != null)
                session.close();
        }

        return objects;
    }

    @Override
    public Action findActionByCode(String code) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        Action object = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(Action.class);
            criteria.add(Restrictions.eq("tdCode", code));

            object = (Action) criteria.uniqueResult();
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);

        } catch (Exception e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);

        } finally {
            if (session != null)
                session.close();
        }

        return object;
    }

    @Override
    public void updateCr(Long actionId, String crNumber, String crName, Date startTime, Date endTime, Long crState, String userExecute) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

//            String sql = "update ACTION SET CR_NUMBER=:crNumber, CR_NAME=:crName where ID=:actionId";
//            logger.info(crName + "\t" + crState);
            String sql = "update ACTION SET CR_NUMBER=:crNumber, CR_NAME=:crName, BEGIN_TIME=:beginTime, END_TIME=:endTime, LINK_CR_TIME=:linkTime, CR_STATE=:crState" + (StringUtils.isNotEmpty(userExecute) ? ", USER_EXECUTE=:userExecute" : "") + " where ID=:actionId";
            SQLQuery query = session.createSQLQuery(sql);
            query.setParameter("actionId", actionId);
            query.setParameter("crNumber", crNumber);
            query.setParameter("crName", crName);
            query.setParameter("beginTime", startTime);
            query.setParameter("endTime", endTime);
            query.setParameter("linkTime", new Date());
            query.setParameter("crState", crState);
            if (StringUtils.isNotEmpty(userExecute)) {
                query.setParameter("userExecute", userExecute);
            }
            query.executeUpdate();

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public void updateCrNumber(Long actionId, String crNumber) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

//            String sql = "update ACTION SET CR_NUMBER=:crNumber, CR_NAME=:crName where ID=:actionId";
//            logger.info(crName + "\t" + crState);
            String sql = "update ACTION SET CR_NUMBER=:crNumber where ID=:actionId";
            SQLQuery query = session.createSQLQuery(sql);
            query.setParameter("actionId", actionId);
            query.setParameter("crNumber", crNumber);
            query.executeUpdate();

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<Long> findListAppIds(Long actionId) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;

        List<Long> appIds = new ArrayList<>();
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

//            String sql = "select DISTINCT(module_id) from ACTION_DETAIL_APP where ACTION_ID=:actionId";
            String sql = "select DISTINCT(module_id) from ACTION_MODULE where ACTION_ID=:actionId";
            SQLQuery query = session.createSQLQuery(sql);
            query.setParameter("actionId", actionId);

            appIds = query.list();

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return appIds;
    }

    @Override
    public List<Action> findByTime(Date impactTime) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        List<Action> objects = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(Action.class);
            if (impactTime != null) {
                criteria.add(Restrictions.le("beginTime", impactTime));
                criteria.add(Restrictions.ge("endTime", impactTime));
            }
            criteria.add(Restrictions.isNotNull("staffCode"));
            criteria.add(Restrictions.isNull("runStatus"));

            objects = criteria.list();
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);

        } catch (Exception e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);

        } finally {
            if (session != null)
                session.close();
        }

        return objects;
    }

    @Override
    public BigDecimal nextFileSeq() throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        BigDecimal seq = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            String sql = "SELECT FILE_SEQ.nextval FROM DUAL";
            SQLQuery query = session.createSQLQuery(sql);
            seq = (BigDecimal) query.uniqueResult();

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return seq;
    }

    @Override
    public List<Action> findList(int first, int pageSize, Map<String, Object> filters, Map<String, String> orders, List<Long> actionTypes) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        List<Action> objects = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(domainClass);
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

            // Xu ly filter.

            setCriteriaRestrictions(criteria, filters);
            if (!actionTypes.isEmpty()) {
                criteria.add(Restrictions.in("actionType", actionTypes));
            }

            // Xu ly order
            setCriteriaOrders(criteria, orders);

            // Xu ly paging
            criteria.setFirstResult(first);
            criteria.setMaxResults(pageSize);

            objects = criteria.list();
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            logger.debug(e.getMessage());
            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            logger.debug(e.getMessage());
            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null)
                session.close();
        }
        return objects;
    }

    @Override
    public int count(Map<String, Object> filters, List<Long> actionTypes) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        int count = 0;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(domainClass);

            // Xu ly filter.
            setCriteriaRestrictions(criteria, filters);
            if (!actionTypes.isEmpty()) {
                criteria.add(Restrictions.in("actionType", actionTypes));
            }

            criteria.setProjection(Projections.rowCount());
            count = criteria.uniqueResult() == null ? 0 : ((Long) criteria.uniqueResult()).intValue();
            session.flush();
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            logger.debug(e.getMessage());
            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            logger.debug(e.getMessage());
            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null)
                session.close();
        }
        return count;
    }

    @Override
    public Long updateVoResult(String signStatus, String documentCode, String lastSignEmail, String publishDate,
                               Long voTextId, String transCode) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;

        Long result = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            String sql = "update ACTION SET SIGN_STATUS=:signStatus, DOCUMENT_CODE=:documentCode, LAST_SIGN_EMAIL=:lastSignEmail, PUBLISH_DATE=:publishDate, VO_TEXT_ID=:voTextId where TD_CODE=:transCode";
            SQLQuery query = session.createSQLQuery(sql);
            query.setParameter("signStatus", signStatus);
            query.setParameter("documentCode", documentCode);
            query.setParameter("lastSignEmail", lastSignEmail);
            query.setParameter("publishDate", publishDate);
            query.setParameter("voTextId", voTextId);
            query.setParameter("transCode", transCode);
            result = Long.valueOf(query.executeUpdate());

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return result;
    }

    @Override
    public List<Action> findCrUctt(Date currentDate, String username) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        List<Action> objects = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(domainClass);
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

            // Xu ly filter.

            criteria.add(Restrictions.eq("createdBy", username));
            criteria.add(Restrictions.eq("crState", Long.valueOf(AamConstants.CR_STATE_UCTT_APPROVE)));

            criteria.add(Restrictions.and(Restrictions.le("beginTime", currentDate), Restrictions.ge("endTime", currentDate)));

            objects = criteria.list();
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            logger.debug(e.getMessage(), e);
            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            logger.debug(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null)
                session.close();
        }
        return objects;
    }

    public void updateRollbackCr(Long actionId, String userApprove, String reasonRollback, String userRollback, Date startTime, Date endTime)
            throws AppException, SysException
    {
        Session session = null;
        Transaction tx = null;
        try
        {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            String sql = "update ACTION SET APPROVE_ROLLBACK_BY=:userApprove, REASON_ROLLBACK=:reasonRollback, EXE_ROLLBACK=:userRollback, START_TIME_ROLLBACK=:startTime, END_TIME_ROLLBACK=:endTime where ID=:actionId";
            SQLQuery query = session.createSQLQuery(sql);
            query.setParameter("actionId", actionId);
            query.setParameter("userApprove", userApprove);
            query.setParameter("reasonRollback", reasonRollback);
            query.setParameter("userRollback", userRollback);
            query.setParameter("startTime", startTime);
            query.setParameter("endTime", endTime);
            query.executeUpdate();

            tx.commit();
        }
        catch (HibernateException e)
        {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new AppException();
        }
        catch (Exception e)
        {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new SysException();
        }
        finally
        {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public void updateImpactUcttCr(Long actionId, String userApprove, String reason, String user, Date startTime, Date endTime)
            throws AppException, SysException
    {
        Session session = null;
        Transaction tx = null;
        try
        {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            String sql = "update ACTION SET APPROVE_ROLLBACK_BY=:userApprove, REASON_IMPACT_UCTT=:reason, EXE_IMPACT_UCTT=:user, START_TIME_IMPACT_UCTT=:startTime, END_TIME_IMPACT_UCTT=:endTime where ID=:actionId";
            SQLQuery query = session.createSQLQuery(sql);
            query.setParameter("actionId", actionId);
            query.setParameter("userApprove", userApprove);
            query.setParameter("reason", reason);
            query.setParameter("user", user);
            query.setParameter("startTime", startTime);
            query.setParameter("endTime", endTime);
            query.executeUpdate();

            tx.commit();
        }
        catch (HibernateException e)
        {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new AppException();
        }
        catch (Exception e)
        {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new SysException();
        }
        finally
        {
            if (session != null) {
                session.close();
            }
        }
    }

    /*20181023_hoangnd_approval impact step_start*/
    public void updateImpactStep(Long actionId, String exeImpactStep, String reasonImpactStep)
            throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            String sql = "update ACTION SET EXE_IMPACT_STEP=:exeImpactStep, REASON_IMPACT_STEP=:reasonImpactStep where ID=:actionId";
            SQLQuery query = session.createSQLQuery(sql);
            query.setParameter("actionId", actionId);
            query.setParameter("exeImpactStep", exeImpactStep);
            query.setParameter("reasonImpactStep", reasonImpactStep);
            query.executeUpdate();

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    /*20181023_hoangnd_approval impact step_end*/

    public void updateBlockRollbackCr(Long actionId, Long typeBlockRollback, String reasonBlockRollback, Date startTime, Date endTime)
            throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            StringBuilder sql = new StringBuilder();
            sql.append(" update ACTION SET TYPE_BLOCK_ROLLBACK = :typeBlockRollback, ");
            if (typeBlockRollback != null && typeBlockRollback.equals(1L)) {
                sql.append(" REASON_BLOCK_ROLLBACK = :reasonBlockRollback, TIME_BLOCK_ROLLBACK_FROM = :startTime, TIME_BLOCK_ROLLBACK_TO = :endTime ");
            } else if (typeBlockRollback != null && typeBlockRollback.equals(2L)) {
                sql.append(" REASON_BLOCK_ROLLBACK = :reasonBlockRollback, TIME_BLOCK_ROLLBACK_FROM = NULL, TIME_BLOCK_ROLLBACK_TO = NULL ");
            } else {
                sql.append(" REASON_BLOCK_ROLLBACK = NULL, TIME_BLOCK_ROLLBACK_FROM = NULL, TIME_BLOCK_ROLLBACK_TO = NULL ");
            }
            sql.append(" where ID = :actionId ");
            SQLQuery query = session.createSQLQuery(sql.toString());
            query.setParameter("actionId", actionId);
            query.setParameter("typeBlockRollback", typeBlockRollback);
            if (typeBlockRollback != null && (typeBlockRollback.equals(1L) || typeBlockRollback.equals(2L))) {
                query.setParameter("reasonBlockRollback", reasonBlockRollback);
                if (typeBlockRollback.equals(1L)) {
                    query.setParameter("startTime", startTime);
                    query.setParameter("endTime", endTime);
                }
            }
            query.executeUpdate();

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public List<Action> findCrUnclosed()
            throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        List<Action> objects = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(this.domainClass);
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

            criteria.add(Restrictions.isNotNull("linkCrTime"));
            criteria.add(Restrictions.not(Restrictions.in("crState", Arrays.asList(new Long[]{Long.valueOf(9L), Long.valueOf(10L)}))));

            objects = criteria.list();
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.debug(e.getMessage());
            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.debug(e.getMessage());
            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return objects;
    }

    public void updateCrFromGnoc(Long actionId, Date startTime, Date endTime, Long crState)
            throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            String sql = "update ACTION SET BEGIN_TIME=:beginTime, END_TIME=:endTime, CR_STATE=:crState where ID=:actionId";
            SQLQuery query = session.createSQLQuery(sql);
            query.setParameter("actionId", actionId);
            query.setParameter("beginTime", startTime);
            query.setParameter("endTime", endTime);
            query.setParameter("crState", crState);
            query.executeUpdate();

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<Action> findCrToExecute(DateTime currentDateTime, String username, Boolean isLoadTest) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        List<Action> objects = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(domainClass);
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

            // Xu ly filter.

            Conjunction normal = Restrictions.and(Restrictions.eq("crState", 6L), Restrictions.le("beginTime", currentDateTime.plusHours(2).toDate()), Restrictions.ge("endTime", currentDateTime.toDate()));
            Conjunction uctt = Restrictions.and(Restrictions.eq("actionType", AamConstants.ACTION_TYPE.ACTION_TYPE_CR_UCTT), Restrictions.eq("createdBy", username), Restrictions.le("beginTime", currentDateTime.plusHours(2).toDate()), Restrictions.ge("endTime", currentDateTime.toDate()));
            Conjunction rollback = Restrictions.and(Restrictions.eq("exeRollback", username), Restrictions.le("startTimeRollback", currentDateTime.toDate()), Restrictions.ge("endTimeRollback", currentDateTime.toDate()));
            Conjunction approveUctt = Restrictions.and(Restrictions.eq("exeImpactUctt", username), Restrictions.le("startTimeImpactUctt", currentDateTime.toDate()), Restrictions.ge("endTimeImpactUctt", currentDateTime.toDate()));
            Conjunction testbed = Restrictions.and(Restrictions.eq("createdBy", username), Restrictions.eq("includeTestbed", 1), Restrictions.eq("runTestbedStatus", 0), Restrictions.ge("endTime", currentDateTime.toDate()));

            Conjunction test = Restrictions.and(new Criterion[]{Restrictions.eq("actionType", AamConstants.ACTION_TYPE.ACTION_TYPE_CR_NORMAL), Restrictions.ilike("crNumber", "TEST_VAS_%")});

            criteria.add(Restrictions.ne("actionType", AamConstants.ACTION_TYPE.ACTION_TYPE_KB_UCTT));
            if (isLoadTest)
                criteria.add(Restrictions.or(normal, uctt, rollback, approveUctt, testbed, test));
            else
                criteria.add(Restrictions.or(normal, uctt, rollback, approveUctt, testbed));

            /*20181011_hoangnd_continue fail step_start*/
            criteria.addOrder(Order.desc("id"));
            /*20181011_hoangnd_continue fail step_end*/
            objects = criteria.list();
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            logger.debug(e.getMessage());
            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            logger.debug(e.getMessage());
            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null)
                session.close();
        }
        return objects;
    }

    @Override
    //20190826_tudn_start lap lich tac dong tu dong GNOC
//    public void updateAutoRunCr(Long actionId, Boolean autoRun) throws AppException, SysException {
    public void updateAutoRunCr(Long actionId, Boolean autoRun, Long typeConfirmGNOC, Long typeRunGNOC, String crLinkGNOC, Long cfStatusNocpro) throws AppException, SysException {
        //20190826_tudn_end lap lich tac dong tu dong GNOC
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            //20190826_tudn_start lap lich tac dong tu dong GNOC
//            String sql = "update ACTION SET RUN_AUTO=:autoRun where ID=:actionId";
            String sql = "";
            if (cfStatusNocpro == null) {
                sql = "update ACTION SET RUN_AUTO=:autoRun"
                        + ((typeConfirmGNOC == null || typeConfirmGNOC.equals("")) ? "" : ", TYPE_CONFIRM_GNOC=:typeConfirmGNOC")
                        + ((typeRunGNOC == null || typeRunGNOC.equals("")) ? "" : ", TYPE_RUN_GNOC=:typeRunGNOC")
                        + ((crLinkGNOC == null || crLinkGNOC.equals("")) ? "" : ", CR_LINK_GNOC=:crLinkGNOC") + " where ID=:actionId";
//                sql = "update ACTION SET RUN_AUTO=:autoRun,TYPE_CONFIRM_GNOC=:typeConfirmGNOC,TYPE_RUN_GNOC=:typeRunGNOC,CR_LINK_GNOC=:crLinkGNOC where ID=:actionId";
            } else {
                sql = "update ACTION SET CF_STATUS_NOCPRO=:cfStatusNocpro where ID=:actionId";
            }
            //20190826_tudn_end lap lich tac dong tu dong GNOC
            SQLQuery query = session.createSQLQuery(sql);
            query.setParameter("actionId", actionId);
            //20190826_tudn_start lap lich tac dong tu dong GNOC
            if (cfStatusNocpro == null) {
                query.setParameter("autoRun", (autoRun != null && autoRun) ? 1L : 0L);
                if(typeConfirmGNOC != null && !typeConfirmGNOC.equals("")) {
                    query.setParameter("typeConfirmGNOC", typeConfirmGNOC);
                }
                if(typeRunGNOC != null && !typeRunGNOC.equals("")) {
                    query.setParameter("typeRunGNOC", typeRunGNOC);
                }
                if(crLinkGNOC != null && !crLinkGNOC.equals("")) {
                    query.setParameter("crLinkGNOC", crLinkGNOC);
                }
            } else {
                query.setParameter("cfStatusNocpro", cfStatusNocpro);
            }
            //20190826_tudn_end lap lich tac dong tu dong GNOC
            query.executeUpdate();

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public void updateRunning(Long actionId, Date actualStartTime, Integer runningStatus) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            String sql = "update ACTION SET RUNNING_STATUS=:runningStatus" + (actualStartTime == null ? "" : ", ACTUAL_START_TIME=:actualStartTime") + " where ID=:actionId";
            SQLQuery query = session.createSQLQuery(sql);
            query.setParameter("actionId", actionId);
            query.setParameter("runningStatus", runningStatus);
            if (actualStartTime != null)
                query.setParameter("actualStartTime", actualStartTime);
            query.executeUpdate();

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public int countWaiting(Long actionId) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        int count;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            String sql = "select count(*) from ACTION_CUSTOM_ACTION a join ACTION_CUSTOM_GROUP g on a.GROUP_ID=g.ID and a.TYPE=3 and g.ACTION_ID=:actionId";
            SQLQuery query = session.createSQLQuery(sql);
            query.setParameter("actionId", actionId);
            count = ((BigDecimal) query.uniqueResult()).intValue();

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return count;
    }

    @Override
    public List<String> findIpReboot(Long actionId) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        List<String> ipServers;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            String sql = "select IP_SERVER from ACTION_SERVER where ACTION_ID=:actionId";
            SQLQuery query = session.createSQLQuery(sql);
            query.setParameter("actionId", actionId);
            ipServers = (List<String>) query.list();

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return ipServers;
    }

    @Override
    public List<Action> findCrAuto(Date currentDate) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        List<Action> actions;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            Criteria criteria = session.createCriteria(domainClass);
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            criteria.add(Restrictions.eq("runAuto", 1));
            criteria.add(Restrictions.eq("crState", 6L));
            criteria.add(Restrictions.isNull("runStatus"));
            criteria.add(Restrictions.le("beginTime", currentDate));
            criteria.add(Restrictions.ge("endTime", currentDate));

            actions = criteria.list();

           /* String sql = "select * from action where RUN_AUTO=1 and cr_state=6 and RUN_STATUS is null and :currentDate BETWEEN BEGIN_TIME and END_TIME";
            SQLQuery query = session.createSQLQuery(sql);
            query.setTimestamp("currentDate", currentDate);
            actions = (List<Action>) query.list();*/

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return actions;
    }

    /*20180813_hoangnd_send mail/sms action ci/cd_start*/
    @Override
    public List<Action> findCiCdNextRun(Date currDate, String timeBeforeRun) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        List<Action> actions;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            SQLQuery query = session.createSQLQuery(" select * from ACTION a " +
                    " where a.TYPE_CI_CD = 1 and a.RUNNING_STATUS is null " +
                    " and a.BEGIN_TIME - interval '" + timeBeforeRun + "' minute < :currDate and :currDate < a.BEGIN_TIME ");
            query.addEntity(Action.class);
            query.setTimestamp("currDate", currDate);

            actions = query.list();

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return actions;
    }

    /*20180912_hoangnd_dong bo work log_start*/
    @Override
    public List<Action> findCiCdRun() throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        List<Action> actions;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            SQLQuery query = session.createSQLQuery(" select * from ACTION a " +
                    " where a.TYPE_CI_CD = 1 and a.RUNNING_STATUS is null and a.CR_STATE = 6 ");
            query.addEntity(Action.class);

            actions = query.list();

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return actions;
    }
    /*20180912_hoangnd_dong bo work log_end*/

    @Override
    public List<Action> findCiCdSuccess(Date currDate) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        List<Action> actions;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            Criteria criteria = session.createCriteria(domainClass);
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

            criteria.add(Restrictions.eq("typeCiCd", 1));
            criteria.add(Restrictions.eq("runningStatus", 1));
            criteria.add(Restrictions.le("beginTime", currDate));
            criteria.add(Restrictions.ge("endTime", currDate));

            actions = criteria.list();

            /*SQLQuery query = session.createSQLQuery(" select * from ACTION a " +
                    " where a.TYPE_CI_CD = 1 and a.RUNNING_STATUS = 1 " +
                    " and a.BEGIN_TIME < :currDate and :currDate < a.END_TIME ");
            query.addEntity(Action.class);
            query.setTimestamp("currDate", currDate);

            actions = query.list();*/

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return actions;
    }
    /*20180813_hoangnd_send mail/sms action ci/cd_end*/

    @Override
    public List<Integer> findKbGroups(Long actionId) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        List<Integer> kbGroups;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            String sql = "select DISTINCT(kb_group) as kbGroup from (select DISTINCT (kb_group), ACTION_ID from ACTION_MODULE where TESTBED_MODE != 1 " +
                    "UNION select DISTINCT (kb_group), ACTION_ID from ACTION_DETAIL_DATABASE where TESTBED_MODE != 1 " +
                    "UNION select DISTINCT (kb_group), ACTION_ID from ACTION_CUSTOM_GROUP) where ACTION_ID=:actionId";

            SQLQuery query = session.createSQLQuery(sql);
            query.setParameter("actionId", actionId);
            query.addScalar("kbGroup", new IntegerType());
            kbGroups = (List<Integer>) query.list();

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return kbGroups;
    }

    @Override
    public List<Action> findCrByUser(String username, List<Long> crStates, Date startTime, Date endTime, Boolean includeAutoTicket) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        List<Action> actions;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            Criteria criteria = session.createCriteria(domainClass);
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            if (includeAutoTicket) {
                criteria.add(Restrictions.in("userExecute", Arrays.asList(username, "SYSTEM")));
            } else {
                criteria.add(Restrictions.eq("userExecute", username));
            }
            if (!crStates.isEmpty())
                criteria.add(Restrictions.in("crState", crStates));
                /*20181015_hoangnd_fix filter theo cr state_start*/
            else {
                Criterion orCrState1 = Restrictions.isNull("crState");
                Criterion orCrState2 = Restrictions.ne("crState", 9L);
                Criterion orCrState = Restrictions.or(orCrState1, orCrState2);
                criteria.add(orCrState);
            }
            /*20181015_hoangnd_fix filter theo cr state_end*/
            criteria.add(Restrictions.le("beginTime", endTime));
            criteria.add(Restrictions.ge("beginTime", startTime));

            actions = criteria.list();

           /* String sql = "select * from action where RUN_AUTO=1 and cr_state=6 and RUN_STATUS is null and :currentDate BETWEEN BEGIN_TIME and END_TIME";
            SQLQuery query = session.createSQLQuery(sql);
            query.setTimestamp("currentDate", currentDate);
            actions = (List<Action>) query.list();*/

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return actions;
    }

    @Override
    public List<Action> findCrError() throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        List<Action> actions;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            Criteria criteria = session.createCriteria(domainClass);
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            criteria.add(Restrictions.in("id", Arrays.asList(594l, 604l, 433l, 487l, 632l, 646l, 653l, 656l, 664l, 865l, 765l, 766l, 671l, 942l, 884l, 916l, 929l, 956l, 983l, 991l, 1018l, 1037l, 1356l, 1335l, 1216l, 1187l, 1409l, 1455l, 1467l, 1476l, 1416l, 1485l, 2052l, 1646l, 1903l, 1664l, 1970l, 1975l, 1703l, 1729l, 2137l, 2092l, 2151l, 2295l, 2209l, 2190l, 2495l, 2470l, 2489l, 2340l, 2747l, 2831l, 2866l, 2934l, 2990l, 3140l, 3111l, 3095l, 3132l, 3210l, 3223l, 3245l, 3248l, 3338l, 3428l, 3304l, 3312l, 3534l, 3539l, 510l, 1605l, 3478l, 5745l, 5764l, 5852l, 5892l, 5896l, 4803l, 4794l, 4231l, 4239l, 4282l, 5456l, 5512l, 5560l, 3981l, 4629l, 4642l, 4597l, 4463l, 4739l, 4720l, 4802l, 5048l, 5097l, 5162l, 5067l, 5100l, 5038l, 5349l, 3606l, 5145l, 3832l, 3884l, 4057l, 4143l, 591l, 352l, 448l, 498l, 633l, 655l, 708l, 856l, 871l, 751l, 808l, 828l, 836l, 752l, 845l, 651l, 662l, 683l, 707l, 912l, 895l, 979l, 989l, 1068l, 1334l, 1146l, 1585l, 1368l, 1386l, 1443l, 1458l, 1500l, 1512l, 1660l, 1755l, 1625l, 2046l, 2047l, 1893l, 1871l, 1863l, 1956l, 1690l, 1723l, 1759l, 1785l, 1795l, 2085l, 2268l, 2227l, 2501l, 2544l, 2363l, 2587l, 2315l, 2353l, 2458l, 2357l, 2579l, 2771l, 3003l, 2960l, 3051l, 3062l, 3077l, 2931l, 3129l, 3241l, 3246l, 3260l, 3347l, 3351l, 3450l, 3524l, 3551l, 576l, 1738l, 3479l, 5746l, 5863l, 5889l, 5808l, 5861l, 3634l, 4222l, 3619l, 4228l, 3776l, 4221l, 3636l, 5491l, 5362l, 5536l, 5629l, 5874l, 4686l, 4625l, 4556l, 4626l, 4678l, 4771l, 4907l, 4915l, 4858l, 5242l, 4884l, 5003l, 5191l, 5054l, 5023l, 5293l, 5304l, 5376l, 5383l, 4347l, 3775l, 3849l, 3894l, 3914l, 4127l, 4035l, 612l, 343l, 441l, 462l, 684l, 768l, 645l, 1108l, 954l, 957l, 927l, 1044l, 1057l, 1088l, 1178l, 1242l, 1214l, 1350l, 1158l, 1283l, 1595l, 1407l, 1470l, 1478l, 1540l, 1565l, 1643l, 2044l, 1862l, 1905l, 1907l, 1926l, 1693l, 1762l, 2232l, 2359l, 2385l, 2430l, 2498l, 2705l, 2787l, 2756l, 2930l, 3252l, 3413l, 3443l, 3556l, 1005l, 1933l, 2564l, 3471l, 3493l, 3503l, 5760l, 5862l, 3576l, 3581l, 4288l, 5502l, 5466l, 5701l, 4434l, 4589l, 4639l, 4890l, 5016l, 5439l, 4933l, 5214l, 5346l, 5109l, 5378l, 5380l, 5381l, 3793l, 3592l, 3896l, 3885l, 3922l, 3652l, 4012l, 4024l, 4379l, 4165l, 603l, 342l, 478l, 485l, 518l, 550l, 556l, 634l, 699l, 726l, 741l, 1107l, 1114l, 1129l, 926l, 964l, 1001l, 1011l, 1027l, 1167l, 1142l, 1170l, 1262l, 1315l, 1544l, 1590l, 1473l, 1481l, 1503l, 1518l, 1638l, 1832l, 1825l, 1849l, 1882l, 1615l, 1918l, 1944l, 1968l, 1977l, 2004l, 2010l, 1671l, 1715l, 1717l, 2195l, 2223l, 2262l, 2191l, 2084l, 2384l, 2431l, 2576l, 2591l, 2422l, 2515l, 2413l, 2553l, 2910l, 2790l, 2868l, 2872l, 2880l, 2869l, 2988l, 3053l, 3196l, 3290l, 3381l, 3402l, 3426l, 3442l, 1858l, 1997l, 3480l, 3457l, 5775l, 4815l, 3768l, 4201l, 3610l, 4284l, 4241l, 5537l, 5552l, 5555l, 5577l, 4479l, 4760l, 4853l, 4712l, 4673l, 4879l, 5184l, 5289l, 5297l, 5398l, 4344l, 3794l, 3868l, 3859l, 3902l, 3904l, 3964l, 3965l, 3966l, 3948l, 3840l, 4145l, 4016l, 4025l, 4037l, 3804l, 3998l, 4073l, 472l, 528l, 559l, 569l, 573l, 577l, 714l, 725l, 859l, 867l, 817l, 852l, 887l, 1010l, 1019l, 1051l, 1072l, 1085l, 1093l, 1183l, 1352l, 1281l, 1255l, 1406l, 1456l, 1465l, 1450l, 1493l, 1396l, 1511l, 1541l, 1820l, 1828l, 2031l, 1649l, 1656l, 1937l, 1939l, 1976l, 1718l, 2107l, 2231l, 2347l, 2371l, 2411l, 2716l, 2748l, 2759l, 2786l, 2765l, 3063l, 3050l, 3079l, 3094l, 3195l, 3208l, 3251l, 3254l, 3258l, 3298l, 3327l, 3403l, 3462l, 702l, 931l, 2042l, 2017l, 2024l, 5642l, 5766l, 5821l, 3639l, 3716l, 4289l, 4507l, 4278l, 4291l, 4304l, 5361l, 4495l, 4624l, 4830l, 4861l, 4730l, 4522l, 4688l, 4751l, 4877l, 5447l, 4974l, 5290l, 5360l, 5384l, 3848l, 3872l, 3943l, 3967l, 3910l, 4166l, 4162l, 4169l, 4355l, 4438l, 628l, 479l, 484l, 543l, 553l, 565l, 582l, 724l, 786l, 794l, 809l, 855l, 648l, 706l, 899l, 993l, 1059l, 1070l, 1099l, 1100l, 1200l, 1174l, 1264l, 1282l, 1256l, 1198l, 1599l, 1445l, 1453l, 1561l, 1568l, 1619l, 1657l, 1806l, 2037l, 2040l, 1617l, 2001l, 1681l, 1767l, 1772l, 1773l, 2135l, 2087l, 2220l, 2319l, 2349l, 2451l, 2338l, 2473l, 2509l, 2699l, 2621l, 2725l, 2751l, 2740l, 2818l, 2781l, 3059l, 3130l, 3171l, 3337l, 3432l, 3354l, 477l, 1499l, 2192l, 3460l, 5672l, 5710l, 5894l, 5683l, 5887l, 3642l, 3613l, 5467l, 4303l, 5639l, 5923l, 4719l, 4575l, 4635l, 4840l, 4954l, 5157l, 5002l, 5004l, 5215l, 5303l, 5313l, 5413l, 5414l, 3814l, 3892l, 3961l, 4059l, 4142l, 4389l, 4423l, 595l, 393l, 520l, 537l, 585l, 701l, 697l, 723l, 740l, 790l, 831l, 987l, 1024l, 1049l, 1052l, 1137l, 1151l, 1305l, 1289l, 1318l, 1210l, 1387l, 1398l, 1384l, 1479l, 1362l, 1363l, 1436l, 1502l, 1645l, 1810l, 1830l, 2041l, 2060l, 1881l, 1621l, 1966l, 1972l, 2012l, 1712l, 1716l, 2083l, 2162l, 2149l, 2198l, 2502l, 2560l, 2467l, 2494l, 2712l, 2714l, 2913l, 2767l, 2898l, 3025l, 3028l, 3008l, 3001l, 3267l, 3270l, 3207l, 3397l, 3394l, 3429l, 3449l, 589l, 821l, 3488l, 5706l, 5813l, 5820l, 5891l, 4799l, 4193l, 4505l, 5575l, 5489l, 5866l, 5585l, 5609l, 5692l, 4671l, 4663l, 4855l, 5041l, 5140l, 4951l, 5420l, 4466l, 5085l, 5063l, 5366l, 5423l, 4345l, 4337l, 3605l, 3722l, 3736l, 3927l, 4017l, 4010l, 4150l, 4052l, 4163l, 4176l, 3769l, 5752l, 4420l, 593l, 381l, 374l, 452l, 398l, 570l, 588l, 654l, 705l, 742l, 754l, 820l, 642l, 1130l, 928l, 940l, 882l, 908l, 930l, 959l, 980l, 1004l, 1006l, 1096l, 1136l, 1165l, 1357l, 1279l, 1280l, 1327l, 1134l, 1604l, 1395l, 1426l, 1547l, 1555l, 1624l, 1894l, 1610l, 1661l, 1739l, 2166l, 2208l, 2390l, 2510l, 2525l, 2595l, 2789l, 2802l, 2834l, 2845l, 3018l, 3216l, 3114l, 3089l, 3220l, 3349l, 3356l, 3325l, 3382l, 3408l, 955l, 1098l, 1543l, 3507l, 5886l, 5825l, 5898l, 5510l, 3631l, 4178l, 4280l, 5451l, 4315l, 4319l, 5507l, 4325l, 5696l, 5590l, 5594l, 4477l, 4576l, 4617l, 4722l, 4681l, 4756l, 4948l, 5147l, 5181l, 5192l, 5338l, 3597l, 3933l, 4021l, 4075l, 4173l, 4049l, 4395l, 4372l)));

            actions = criteria.list();

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return actions;
    }

    @Override
    public List<String> findModuleTypeNotDb() throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        List<String> moduleTypes;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            String sql = "select DISTINCT (module_type) as moduleType from EXCLUDE_MODULE_MAINTAIN where STATUS=0";

            SQLQuery query = session.createSQLQuery(sql);
            query.addScalar("moduleType", new StringType());
            moduleTypes = (List<String>) query.list();

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return moduleTypes;
    }

    @Override
    public void updateRunStatus(Long actionId, Integer status) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            if (actionId != null) {
                if (status == null) {
                    String sql = "update ACTION SET RUN_STATUS =null where ID=:actionId";
                    SQLQuery query = session.createSQLQuery(sql);
                    query.setParameter("actionId", actionId);
                    query.executeUpdate();
                } else {
                    String sql = "update ACTION SET RUN_STATUS =:runStatus where ID=:actionId";
                    SQLQuery query = session.createSQLQuery(sql);
                    query.setParameter("actionId", actionId);
                    query.setParameter("runStatus", status);
                    query.executeUpdate();
                }
            }

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            logger.error(e.getMessage(), e);
            throw new AppException();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
