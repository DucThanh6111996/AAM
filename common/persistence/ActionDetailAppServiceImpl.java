package com.viettel.persistence;

// Created May 5, 2016 4:56:37 PM by quanns2

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.controller.ActionDetailApp;
import com.viettel.util.HibernateUtil;
import com.viettel.controller.TextUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Service implement for interface ActionDetailAppService.
 *
 * @author quanns2
 * @see com.viettel.persistence.ActionDetailAppService
 */

@Service(value = "actionDetailAppService")
@Scope("session")
public class ActionDetailAppServiceImpl extends GenericDaoImpl<ActionDetailApp, Long>
        implements ActionDetailAppService, Serializable {
    private static Logger logger = LogManager.getLogger(ActionDetailAppServiceImpl.class);

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public List<ActionDetailApp> findListDetailApp(Long actionId, String groupAction, List<Long> moduleIds, Boolean includeTestbed) {
        return findListDetailApp(actionId, groupAction, true, null, moduleIds, includeTestbed);
    }

    public List<ActionDetailApp> findListDetailApp(Long actionId, String groupAction, Long moduleId, List<Long> moduleIds, Boolean includeTestbed) {
        return findListDetailApp(actionId, groupAction, true, moduleId, moduleIds, includeTestbed);
    }

    public List<ActionDetailApp> findListDetailApp(Long actionId, String groupAction, boolean order, List<Long> moduleIds, Boolean includeTestbed) {
        return findListDetailApp(actionId, groupAction, order, null, moduleIds, includeTestbed);
    }

    @Override
    public void updateRunStatus(Long id, Integer runStatus, Date startTime, Date endTime)
            throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            if (id != null) {
                String sql = "update ACTION_DETAIL_APP SET RUN_STATUS=:runStatus";
                if (startTime != null) {
                    sql += " , RUN_START_TIME = :startTime  ";
                }
                if (endTime != null) {
                    sql += " , RUN_END_TIME = :endTime  ";
                }
                sql += " where ID=:id";

                SQLQuery query = session.createSQLQuery(sql);
                query.setParameter("id", id);
                query.setParameter("runStatus", runStatus);
                if (startTime != null) {
                    query.setTimestamp("startTime", startTime);
                }
                if (endTime != null) {
                    query.setTimestamp("endTime", endTime);
                }

                query.executeUpdate();
                TextUtils.checkAndPrintObject(logger,"updateRunStatus","id",id,
                        "runStatus",runStatus ,
                        "startTime",startTime ,
                        "startTime",startTime ,
                        "endTime",endTime ,
                        "result","TRUE");
            }

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            TextUtils.checkAndPrintObject(logger,"updateRunStatus","id",id,
                    "runStatus",runStatus ,
                    "startTime",startTime ,
                    "startTime",startTime ,
                    "endTime",endTime ,
                    "result","FALSE");
            throw new AppException();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            TextUtils.checkAndPrintObject(logger,"updateRunStatus","id",id,
                    "runStatus",runStatus ,
                    "startTime",startTime ,
                    "startTime",startTime ,
                    "endTime",endTime ,
                    "result","FALSE");
            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public void updateBackupStatus(Long id, Integer backupStatus, Date startTime, Date endTime)
            throws AppException, SysException {

        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            if (id != null) {
                String sql = "update ACTION_DETAIL_APP SET BACKUP_STATUS=:backupStatus ";
                if (startTime != null) {
                    sql += " , BACKUP_START_TIME = :startTime  ";
                }
                if (endTime != null) {
                    sql += " , BACKUP_END_TIME = :endTime  ";
                }
                sql += " where ID=:id";
                SQLQuery query = session.createSQLQuery(sql);
                query.setParameter("id", id);
                query.setParameter("backupStatus", backupStatus);
                if (startTime != null) {
                    query.setTimestamp("startTime", startTime);
                }
                if (endTime != null) {
                    query.setTimestamp("endTime", endTime);
                }
                query.executeUpdate();
            }

            tx.commit();
            TextUtils.checkAndPrintObject(logger,"updateBackupStatus","id",id,
                    "backupStatus",backupStatus ,
                    "startTime",startTime ,
                    "endTime",endTime ,
                    "result","TRUE");
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            TextUtils.checkAndPrintObject(logger,"updateBackupStatus","id",id,
                    "backupStatus",backupStatus ,
                    "startTime",startTime ,
                    "endTime",endTime ,
                    "result","FALSE");
            throw new AppException();
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
            TextUtils.checkAndPrintObject(logger,"updateBackupStatus","id",id,
                    "backupStatus",backupStatus ,
                    "startTime",startTime ,
                    "endTime",endTime ,
                    "result","FALSE");
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public List<ActionDetailApp> findListDetailApp(Long actionId, String groupAction, boolean orderAsc, Long moduleId, List<Long> moduleIds, Boolean includeTestbed) {

        Session session = null;
        Transaction tx = null;
        List<ActionDetailApp> objects = null;

        if (moduleIds != null && moduleIds.isEmpty()) {
            TextUtils.checkAndPrintObject(logger,"actionId",actionId,
                    "groupAction",groupAction ,
                    "orderAsc",orderAsc ,
                    "moduleId",moduleId ,
                    "moduleIds",moduleIds ,
                    "includeTestbed",includeTestbed,
                    "result","");
            return new ArrayList<>();
        }
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(ActionDetailApp.class);
            criteria.add(Restrictions.eq("actionId", actionId));
            if (StringUtils.isNotEmpty(groupAction))
                criteria.add(Restrictions.eq("groupAction", groupAction));

            if (moduleIds != null)
                criteria.add(Restrictions.in("moduleId", moduleIds));

            if (orderAsc)
                criteria.addOrder(Order.asc("moduleOrder"));
            else
                criteria.addOrder(Order.desc("moduleOrder"));

            if (moduleId != null)
                criteria.add(Restrictions.eq("moduleId", moduleId));

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

        TextUtils.checkAndPrintObject(logger,"findListDetailApp","actionId",actionId,
                "groupAction",groupAction ,
                "orderAsc",orderAsc ,
                "moduleId",moduleId ,
                "moduleIds",moduleIds ,
                "includeTestbed",includeTestbed,
                "result",objects);
        return objects;

    }

    @Override
    public List<ActionDetailApp> findListDetailApp(Long actionId) {
        return findListDetailApp(actionId, null, true, null, true);
    }
}
