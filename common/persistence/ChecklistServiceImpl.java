package com.viettel.persistence;

// Created May 5, 2016 4:56:37 PM by quanns2

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.ActionModuleChecklist;
import com.viettel.model.Checklist;
import com.viettel.util.HibernateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.*;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Service implement for interface ChecklistService.
 *
 * @author quanns2
 * @see TimeZoneService
 */

@Service(value = "checklistService")
@Scope("session")
public class ChecklistServiceImpl extends GenericDaoImpl<Checklist, Serializable> implements ChecklistService, Serializable {
    private static Logger logger = LogManager.getLogger(TimeZoneServiceImpl.class);

    @Override
    public List<Checklist> findCheckListAppByAction(Long actionId) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        List<Checklist> list = new ArrayList<>();
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            /*20181116_hoangnd_save all step_start*/
            String sql = "select c.CHECKLIST_ID id, m.MODULE_ID moduleId, c.ID actionModuleChecklistId, " +
                    "  c.STATUS_BEFORE statusBefore, c.STATUS_IMPACT statusImpact, c.STATUS_AFTER statusAfter, c.STATUS_ROLLBACK statusRollback, " +
                    "  c.RESULT_BEFORE resultBefore, c.RESULT_IMPACT resultImpact, c.RESULT_AFTER resultAfter, c.RESULT_ROLLBACK resultRollback, " +
                    "  c.LIMITED_BEFORE limitedBefore, c.LIMITED_IMPACT limitedImpact, c.LIMITED_AFTER limitedAfter, c.LIMITED_ROLLBACK limitedRollback, " +
                    "  k.CODE code, k.NAME name, k.TYPE type, k.CHECKOUT_TYPE checkoutType, k.NUMBER_DEFAULT_VALUE numberDefaultValue, " +
                    "  k.DEFAULT_MATH_OPTION defaultMathOption, k.EXCEPTION_VALUE exceptionValue, k.STRING_DEFAULT_VALUE stringDefaultValue " +
                    " from ACTION_MODULE_CHECKLIST c " +
                    "  join ACTION_MODULE m on c.ACTION_MODULE_ID=m.ID " +
                    "  join SPCL_NEW_KPI k on k.ID=c.CHECKLIST_ID " +
                    " where m.ACTION_ID=:actionId";
            /*20181116_hoangnd_save all step_end*/
            Query query = session.createSQLQuery(sql).addScalar("id", StandardBasicTypes.LONG)
            .addScalar("moduleId", StandardBasicTypes.LONG)
            /*20181116_hoangnd_save all step_start*/
            .addScalar("actionModuleChecklistId", StandardBasicTypes.LONG)
            .addScalar("code", StandardBasicTypes.STRING)
            .addScalar("name", StandardBasicTypes.STRING)
            .addScalar("type", StandardBasicTypes.INTEGER)
            .addScalar("checkoutType", StandardBasicTypes.INTEGER)
            .addScalar("numberDefaultValue", StandardBasicTypes.FLOAT)
            .addScalar("defaultMathOption", StandardBasicTypes.INTEGER)
            .addScalar("exceptionValue", StandardBasicTypes.STRING)
            .addScalar("stringDefaultValue", StandardBasicTypes.STRING)
            .addScalar("statusBefore", StandardBasicTypes.STRING)
            .addScalar("statusImpact", StandardBasicTypes.STRING)
            .addScalar("statusAfter", StandardBasicTypes.STRING)
            .addScalar("statusRollback", StandardBasicTypes.STRING)
            .addScalar("resultBefore", StandardBasicTypes.STRING)
            .addScalar("resultImpact", StandardBasicTypes.STRING)
            .addScalar("resultAfter", StandardBasicTypes.STRING)
            .addScalar("resultRollback", StandardBasicTypes.STRING)
            .addScalar("limitedBefore", StandardBasicTypes.STRING)
            .addScalar("limitedImpact", StandardBasicTypes.STRING)
            .addScalar("limitedAfter", StandardBasicTypes.STRING)
            .addScalar("limitedRollback", StandardBasicTypes.STRING)
            /*20181116_hoangnd_save all step_end*/
            .setParameter("actionId", actionId).setResultTransformer(Transformers.aliasToBean(Checklist.class));

            list = query.list();
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
        return list;
    }

    @Override
    public List<Checklist> findCheckListDbByAction(Long actionId) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        List<Checklist> list = new ArrayList<>();
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            /*20181119_hoangnd_save all step_start*/
//            String sql = "select checklist_id id, app_db_id moduleId from ACTION_DB_CHECKLIST where ACTION_ID=:actionId";
            String sql = " select c.CHECKLIST_ID id, c.APP_DB_ID moduleId, c.ID actionDbChecklistId, " +
                    "  c.STATUS_BEFORE statusBefore, c.STATUS_IMPACT statusImpact, c.STATUS_AFTER statusAfter, c.STATUS_ROLLBACK statusRollback, " +
                    "  c.RESULT_BEFORE resultBefore, c.RESULT_IMPACT resultImpact, c.RESULT_AFTER resultAfter, c.RESULT_ROLLBACK resultRollback, " +
                    "  c.LIMITED_BEFORE limitedBefore, c.LIMITED_IMPACT limitedImpact, c.LIMITED_AFTER limitedAfter, c.LIMITED_ROLLBACK limitedRollback " +
                    " from ACTION_DB_CHECKLIST c " +
                    " where c.ACTION_ID=:actionId ";
            /*20181119_hoangnd_save all step_end*/
            Query query = session.createSQLQuery(sql).addScalar("id", StandardBasicTypes.LONG)
                    .addScalar("moduleId", StandardBasicTypes.LONG)
                    /*20181119_hoangnd_save all step_start*/
                    .addScalar("actionDbChecklistId", StandardBasicTypes.LONG)
                    .addScalar("statusBefore", StandardBasicTypes.STRING)
                    .addScalar("statusImpact", StandardBasicTypes.STRING)
                    .addScalar("statusAfter", StandardBasicTypes.STRING)
                    .addScalar("statusRollback", StandardBasicTypes.STRING)
                    .addScalar("resultBefore", StandardBasicTypes.STRING)
                    .addScalar("resultImpact", StandardBasicTypes.STRING)
                    .addScalar("resultAfter", StandardBasicTypes.STRING)
                    .addScalar("resultRollback", StandardBasicTypes.STRING)
                    .addScalar("limitedBefore", StandardBasicTypes.STRING)
                    .addScalar("limitedImpact", StandardBasicTypes.STRING)
                    .addScalar("limitedAfter", StandardBasicTypes.STRING)
                    .addScalar("limitedRollback", StandardBasicTypes.STRING)
                    /*20181119_hoangnd_save all step_end*/
                    .setParameter("actionId", actionId).setResultTransformer(Transformers.aliasToBean(Checklist.class));

            list = query.list();
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
        return list;
    }

    @Override
    public List<Checklist> findCheckListByAction(Collection<Long> ids) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        List<Checklist> list = new ArrayList<>();
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            Criteria criteria = session.createCriteria(domainClass);
            criteria.add(Restrictions.in("id", ids));
            list = (List<Checklist>) criteria.list();

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
        return list;
    }
}
