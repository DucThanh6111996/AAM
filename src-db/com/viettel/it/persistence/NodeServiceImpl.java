/**
 * Created on Tue Sep 06 09:17:09 ICT 2016
 * <p>
 * Copyright (C) 2013 by Viettel Network Company. All rights reserved
 */
package com.viettel.it.persistence;

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.it.model.Node;
import com.viettel.util.HibernateUtil;
import org.hibernate.*;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * NodeRunServiceImpl.java
 *
 * @author Huy, Nguyen Xuan<huynx6@viettel.com.vn>
 * @since Tue Sep 06 09:17:09 ICT 2016
 * @version 1.0.0
 */
@Scope("session")
@Service(value = "nodeService")
public class NodeServiceImpl extends GenericDaoImplNewV2<Node, Long> implements Serializable {
    private static final long serialVersionUID = -4109611148855610L;

    public List<String> findNodeCodeByFlow(Long flowRunId) throws AppException, SysException {
        Session session = null;
        Transaction tx = null;
        List<String> list = new ArrayList<>();
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

			/*20181211_hoangnd_fix sql thanh hql_start*/
            String hql = " select n.nodeCode from NodeRun r left join Node n on n.nodeId = r.node.nodeId where r.id.flowRunId = ? ";

            List nodes = new NodeServiceImpl().findListAll(hql, flowRunId);

            if (nodes != null)
                for (Object node : nodes) {
                    if (node != null) {
                        list.add(node.toString());
                    }
                }
            /*20181211_hoangnd_fix sql thanh hql_end*/
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

    public static void main(String[] args) {
        Session session = null;
        Transaction tx = null;
        List<String> list = new ArrayList<>();
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();

            String sql = "select NODE_CODE nodeCode from NODE_RUN r LEFT JOIN NODE n on r.NODE_ID=n.NODE_ID where r.FLOW_RUN_ID=:flowRunId";

            Query query = session.createSQLQuery(sql)
                    .addScalar("nodeCode", StandardBasicTypes.STRING)
                    .setParameter("flowRunId", 18006l)
                    .setResultTransformer(Transformers.aliasToBean(Node.class));

            List<Node> nodes = query.list();
            if (nodes != null)
                for (Node node : nodes) {
                    System.out.println(node.getNodeCode());
//					list.add(node.getNodeCode());
                }
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();

            logger.error(e.getMessage(), e);
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