package com.viettel.persistence;

// Created May 30, 2016 2:10:12 PM by quanns2

import com.viettel.model.TestCase;
import com.viettel.util.HibernateUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implement for interface TestCaseService.
 *
 * @author quanns2
 * @see TestCaseService
 */

@Service(value = "testCaseService")
@Scope("session")
public class TestCaseServiceImpl extends GenericDaoImpl<TestCase, Serializable> implements TestCaseService, Serializable {
    private static Logger logger = LogManager.getLogger(TestCaseServiceImpl.class);

    public Map<String, String> getMapTestCase(Long actionId) {
        Map<String, String> result = new HashMap<>();
        Session session = null;
        Transaction tx = null;
        List<TestCase> objects = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(TestCase.class);
            criteria.add(Restrictions.eq("actionId", actionId));
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

        if (objects != null) {
            for (TestCase testCase : objects) {
                int tesCaseType = testCase.getTestcaseType();
                switch (tesCaseType) {
                    case 1:
                        result.put("1", FilenameUtils.getName(testCase.getFileName()));
                        break;
                    case 2:
                        result.put("2", FilenameUtils.getName(testCase.getFileName()));
                        break;
                    case 3:
                        result.put("3", FilenameUtils.getName(testCase.getFileName()));
                        break;
                    case 4:
                        result.put("4", FilenameUtils.getName(testCase.getFileName()));
                        break;
                    default:
                        break;
                }
            }
        }

        return result;
    }

    public List<TestCase> getMapTestCaseData(Long actionId) {
        Session session = null;
        Transaction tx = null;
        List<TestCase> objects = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(TestCase.class);
            criteria.add(Restrictions.eq("actionId", actionId));
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
}
