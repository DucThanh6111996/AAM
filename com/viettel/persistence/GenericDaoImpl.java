/*
 * Created on Aug 15, 2013
 *
 * Copyright (C) 2013 by Viettel Network Company. All rights reserved
 */
package com.viettel.persistence;

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.util.HibernateUtil;
import org.apache.commons.lang3.ClassUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.*;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.metadata.ClassMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Id;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.*;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Class implement interface Generic Dao Service
 * 
 * @author Nguyen Hai Ha (hanh45@viettel.com.vn)
 * @since Aug 15, 2013
 * @version 1.0.0
 */
public abstract class GenericDaoImpl<T, PK extends Serializable> implements GenericDao<T, PK> {
	private static final Logger logger = LoggerFactory.getLogger(GenericDaoImpl.class);
	public static final String EXAC = "EXAC";
	public static final String EXAC_IGNORE_CASE = "EXAC_IGNORE_CASE";
	public static final String GE = "GE";
	public static final String GT = "GT";
	public static final String LE = "LE";
	public static final String LT = "LT";
	public static final String NEQ = "NEQ";
	public static final String PREFIX = "-";

	@SuppressWarnings("unchecked")
	protected Class<T> domainClass = (Class<T>) getTypeArguments(GenericDaoImpl.class, this.getClass()).get(0);

	/**
	 * Method to return the class of the domain object
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.viettel.persistence.util.GenericDaoService#findById(java.io.
	 * Serializable)
	 */
	@Override
	public T findById(PK id) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		T object = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			switch (id.getClass().getName()) {
			case "java.lang.String":
				String identifierName = session.getSessionFactory().getClassMetadata(domainClass)
						.getIdentifierPropertyName();
				Criteria criteria = session.createCriteria(domainClass);
				criteria.add(Restrictions.ilike(identifierName, id.toString().toLowerCase(), MatchMode.EXACT));
				object = (T) criteria.uniqueResult();
				break;
			default:
				object = (T) session.get(domainClass, id);
				break;
			}

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
		return (T) object;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.viettel.persistence.util.GenericDaoService#delete(java.lang.Object)
	 */
	@Override
	public void delete(T object) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			session.delete(object);
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.viettel.persistence.util.GenericDaoService#delete(java.util.List)
	 */
	@Override
	public void delete(List<T> objects) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			for (T object : objects)
				session.delete(object);
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.viettel.persistence.util.GenericDaoService#saveOrUpdate(java.lang.
	 * Object)
	 */
	@Override
	public void saveOrUpdate(T object) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			session.saveOrUpdate(object);
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.viettel.persistence.util.GenericDaoService#save(java.lang.Object)
	 */
	@Override
	public PK save(T object) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		PK o = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			o = (PK) session.save(object);
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
		return o;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.viettel.persistence.util.GenericDaoService#saveOrUpdate(java.util.
	 * List)
	 */
	@Override
	public void saveOrUpdate(List<T> objects) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			for (T object : objects)
				session.saveOrUpdate(object);
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.viettel.persistence.util.GenericDaoService#findList()
	 */
	@Override
	public List<T> findList() throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		List<T> objects = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(domainClass);
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.viettel.persistence.util.GenericDaoService#findList(java.util.Map,
	 * java.util.Map)
	 */
	@Override
	public List<T> findList(Map<String, Object> filters, Map<String, String> orders) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		List<T> objects = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(domainClass);
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

			// Xu ly filter.
			setCriteriaRestrictions(criteria, filters);

			// Xu ly order
			setCriteriaOrders(criteria, orders);

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
	public List<T> findList2(Map<String, Object> filters, Map<String, String> orders) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		List<T> objects = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(domainClass);
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

			// Xu ly filter.
			setCriteriaRestrictions2(criteria, filters);

			// Xu ly order
			setCriteriaOrders(criteria, orders);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.viettel.persistence.util.GenericDaoService#findList(int, int,
	 * java.util.Map)
	 */
	@Override
	public List<T> findList(int first, int pageSize, Map<String, Object> filters) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		List<T> objects = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(domainClass);
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

			// Xu ly filter.
			setCriteriaRestrictions(criteria, filters);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.viettel.persistence.util.GenericDaoService#findList(int, int,
	 * java.util.Map, java.util.Map)
	 */
	@Override
	public List<T> findList(int first, int pageSize, Map<String, Object> filters, Map<String, String> orders)
			throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		List<T> objects = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(domainClass);
			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

			// Xu ly filter.
			setCriteriaRestrictions(criteria, filters);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.viettel.persistence.util.GenericDaoService#count(java.util.Map)
	 */
	@Override
	public int count(Map<String, Object> filters) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		int count = 0;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(domainClass);

			// Xu ly filter.
			setCriteriaRestrictions(criteria, filters);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.viettel.persistence.util.GenericDaoService#count()
	 */
	@Override
	public int count() throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		int count = 0;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(domainClass);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.viettel.persistence.util.GenericDaoService#persist(java.lang.Object)
	 */
	@Override
	public void persist(T object) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			session.persist(object);
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.viettel.persistence.util.GenericDaoService#merge(java.lang.Object)
	 */
	@Override
	public void merge(T object) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			session.clear();
			session.merge(object);
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
	}

	@Override
	public T get(PK id) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		T object = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			object = (T) session.get(domainClass, id);
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
		return object;
	}

	/**
	 * Thêm filter cho criteria.
	 * 
	 * @param criteria
	 * @return criteria
	 */
	protected Criteria setCriteriaRestrictions(Criteria criteria, Map<String, Object> filters) {
		if (filters == null)
			return criteria;

		Map<String, String> properties = getFields();

		String type;
		String filedName;
		Object fieldValue;
		Map<String, String> alias = new HashMap<>();

		for (Iterator<String> it = filters.keySet().iterator(); it.hasNext();) {
			filedName = it.next();
			fieldValue = filters.get(filedName);

			String[] fields = filedName.split("\\.");

			type = properties.get(filedName);
			switch (type) {
				case "java.lang.String":
					if (fields.length != 2) {
						if(fieldValue instanceof List<?>){
							criteria.add(Restrictions.in(filedName, (List<?>) fieldValue));
						} else{
							criteria.add(Restrictions.ilike(filedName, fieldValue.toString().toLowerCase(), MatchMode.ANYWHERE));
						}
					} else {
						if (alias.get(fields[0]) == null) {
							criteria.createAlias(fields[0], fields[0] + "alias");
							alias.put(fields[0], fields[0] + "alias");
						}
						// thenv_20180630_them filter theo list_start
						if(fieldValue instanceof List<?>){
							criteria.add(Restrictions.in(fields[0] + "alias." + fields[1], (List<?>) fieldValue));
						} else{
							criteria.add(Restrictions.ilike(fields[0] + "alias." + fields[1], fieldValue.toString().toLowerCase(), MatchMode.ANYWHERE));
						}
						// thenv_20180630_them filter theo list_end
					}
					break;
				case "java.lang.Integer":
					if (fields.length != 2)
						if(fieldValue instanceof List<?>){
							criteria.add(Restrictions.in(filedName, (List<?>) fieldValue));
						} else{
							criteria.add(Restrictions.eq(filedName, Integer.valueOf(fieldValue.toString())));
						}

					else {
						if (alias.get(fields[0]) == null) {
							criteria.createAlias(fields[0], fields[0] + "alias");
							alias.put(fields[0], fields[0] + "alias");
						}
						if(fieldValue instanceof List<?>){
							criteria.add(Restrictions.in(fields[0] + "alias." + fields[1], (List<?>) fieldValue));
						} else{
							criteria.add(Restrictions.eq(fields[0] + "alias." + fields[1], Integer.valueOf(fieldValue.toString())));
						}

					}
					break;
				case "java.lang.Long":
					if (fields.length != 2)
						if(fieldValue instanceof List<?>){
							criteria.add(Restrictions.in(filedName, (List<?>) fieldValue));
						} else{
							criteria.add(Restrictions.eq(filedName, Long.valueOf(fieldValue.toString())));
						}

					else {
						if (alias.get(fields[0]) == null) {
							criteria.createAlias(fields[0], fields[0] + "alias");
							alias.put(fields[0], fields[0] + "alias");
						}
						if(fieldValue instanceof List<?>){
							criteria.add(Restrictions.in(fields[0] + "alias." + fields[1], (List<?>) fieldValue));
						} else{
							criteria.add(Restrictions.eq(fields[0] + "alias." + fields[1], Long.valueOf(fieldValue.toString())));
						}

					}
					break;
				case "java.lang.Boolean":
					if (fields.length != 2)
						criteria.add(Restrictions.eq(filedName, "1".equals(fieldValue)));
					else {
						if (alias.get(fields[0]) == null) {
							criteria.createAlias(fields[0], fields[0] + "alias");
							alias.put(fields[0], fields[0] + "alias");
						}
						criteria.add(Restrictions.eq(fields[0] + "alias." + fields[1], "1".equals(fieldValue)));
					}
					break;
				default:
					break;
			}
		}

		return criteria;
	}

	protected void setCriteriaRestrictions2(Criteria criteria, Map<String, ?> filters) throws ParseException {

		if (filters == null) {
			return;
		}
		String pkName;
		ClassMetadata classMetadata = HibernateUtil.getSessionFactory().getClassMetadata(domainClass);
		pkName = classMetadata.getIdentifierPropertyName();

		Field[] fields = this.domainClass.getDeclaredFields();
		for (Field f : fields) {
			if (f.isAnnotationPresent(Id.class)) {
				pkName = f.getName();
			}
		}

		Map<String, String> properties = getFields();

		String type;
		String colFieldName;
		Object fieldValue;
		for (Iterator<String> itFilter = filters.keySet().iterator(); itFilter.hasNext();) {
			colFieldName = itFilter.next();
			if (Pattern.compile("_FILTER$").matcher(colFieldName).find()) {
				continue;
			}
			String[] colFieldNames = colFieldName.split("-", -1);
			String fieldName = colFieldNames[0];
			String expr = "";
			if (colFieldNames.length == 2)
				expr = colFieldNames[1];
			fieldValue = filters.get(colFieldName);

			if (fieldName.contains(".")) {
				if (fieldName.startsWith(pkName + ".")) {

				} else {
					String[] prs = fieldName.split("\\.", -1);
					for(int i=0; i< prs.length-1; i++){
						String alias = "";
						String aliasNew = "";
						for(int j=0; j<=i; j++){
							alias += prs[j]+ ".";
							aliasNew  += prs[j]+ (j==i-2?".":"");
						}
						alias= alias.replaceAll("\\.$", "");
						Iterator<CriteriaImpl.Subcriteria> iter = ((CriteriaImpl) criteria).iterateSubcriteria();

						if(iter.hasNext()){
							int c = 0;
							while (iter.hasNext()) {
								Criteria subcriteria = iter.next();
								if (subcriteria.getAlias()!=null && subcriteria.getAlias().contains(aliasNew)) {
									c++;
									break;
								}
							}
							if(c==0){
								criteria.createAlias(alias, aliasNew);

							}
						}else{
							criteria.createAlias(alias, aliasNew);

						}
						if(i< prs.length-2)
							fieldName = fieldName.replaceFirst("\\.", "");
					}

					//fieldName = fieldName.replaceAll("^" + alias, alias + "-alias");
//					String alias = fieldName.split("\\.", -1)[0];
//					if (!criteria.getAlias().contains(alias))
//						criteria.createAlias(alias, alias + "-alias");
//					fieldName = fieldName.replaceAll("^" + alias, alias + "-alias");
				}
			}

			type = properties.get(colFieldNames[0]);
			if (type == null)
				throw new IllegalArgumentException("KhÃ´ng tÃ¬m tháº¥y kiá»ƒu dá»¯ liá»‡u cá»§a trÆ°á»�ng tÃ¬m kiáº¿m: " + fieldName);
			switch (type) {
				case "java.lang.String":
					switch (expr) {
						case EXAC:
							if (fieldValue instanceof Object[]) {
								criteria.add(Restrictions.in(fieldName, (Object[]) fieldValue));
							} else if (fieldValue instanceof List<?>) {
								criteria.add(Restrictions.in(fieldName, (List<?>) fieldValue));
							} else if (fieldValue instanceof String) {
								criteria.add(Restrictions.eq(fieldName, (fieldValue)));
							}
							break;
						case EXAC_IGNORE_CASE:
							if (fieldValue instanceof Object[]) {
								fieldValue = Arrays.asList(fieldValue);
							}
							if (fieldValue instanceof List<?>) {
								List<Criterion> cers = new ArrayList<Criterion>();
								for (Object value : (List<?>) fieldValue) {
									if (value instanceof String) {
										Criterion cer = Restrictions.ilike(fieldName, value);
										cers.add(cer);
									}
								}
								criteria.add(Restrictions.or(cers.toArray(new Criterion[cers.size()])));
							} else if (fieldValue instanceof String) {
								criteria.add(Restrictions.ilike(fieldName, (fieldValue)));
							}
							break;
						case NEQ:
							if (fieldValue instanceof String) {
								criteria.add(Restrictions.ne(fieldName, (fieldValue)));
							}else if (fieldValue==null){
								criteria.add(Restrictions.isNotNull(fieldName));
							}
							break;
						default:
							if(fieldValue==null)
								criteria.add(Restrictions.isNull(fieldName));
							else
								criteria.add(Restrictions.ilike(fieldName, ((String) fieldValue).toLowerCase(), MatchMode.ANYWHERE));
							break;
					}
					break;
				case "java.lang.Integer":
				case "java.lang.Long":
				case "java.lang.Double":
				case "java.lang.Boolean":
					if (Arrays.asList(new String[] { LT, GT, LE, GE }).contains(expr)) {
						if (fieldValue instanceof String) {
							Number a = NumberFormat.getInstance().parse((String) fieldValue);
							if ("java.lang.Integer".equals(type))
								fieldValue = a.intValue();
							else
								fieldValue = a;
						}
					} else {
						if (fieldValue instanceof String) {
							Number a = NumberFormat.getInstance().parse((String) fieldValue);
							if ("java.lang.Integer".equals(type))
								fieldValue = a.intValue();
							else
								fieldValue = a;
						} else if (fieldValue instanceof List<?>) {
							List<Object> fieldValueConverted = new ArrayList<>();
							for (Object value : (List<?>) fieldValue) {
								if (value instanceof String) {
									Number value2 = NumberFormat.getInstance().parse((String) value);
									if ("java.lang.Integer".equals(type))
										fieldValueConverted.add(value2.intValue());
									else
										fieldValueConverted.add(value2);
								} else {
									fieldValueConverted.add(value);
								}

							}
							fieldValue = fieldValueConverted;
						} else if (fieldValue instanceof String[]) {
							List<Object> fieldValueConverted = new ArrayList<>();
							for (Object value : (String[]) fieldValue) {
								if (value instanceof String) {
									Number value2 = NumberFormat.getInstance().parse((String) value);
									if ("java.lang.Integer".equals(type))
										fieldValueConverted.add(value2.intValue());
									else
										fieldValueConverted.add(value2);
								}else
									fieldValueConverted.add(value);
							}
							fieldValue = fieldValueConverted;
						}
					}
					switch (expr) {
						case LT:
							if (fieldValue instanceof Number) {
								criteria.add(Restrictions.lt(fieldName, (fieldValue)));
							}
							break;
						case GT:
							if (fieldValue instanceof Number) {
								criteria.add(Restrictions.gt(fieldName, (fieldValue)));
							}
							break;
						case LE:
							if (fieldValue instanceof Number) {
								criteria.add(Restrictions.le(fieldName, (fieldValue)));
							}
							break;
						case GE:
							if (fieldValue instanceof Number) {
								criteria.add(Restrictions.ge(fieldName, (fieldValue)));
							}
							break;
						case NEQ:
							if (fieldValue instanceof Number) {
								criteria.add(Restrictions.ne(fieldName, (fieldValue)));
							}else if (fieldValue==null){
								criteria.add(Restrictions.isNotNull(fieldName));
							}
							break;
						case EXAC:
						default:
							if (fieldValue instanceof Object[]) {
								criteria.add(Restrictions.in(fieldName, (Object[]) fieldValue));
							} else if (fieldValue instanceof List<?>) {
								criteria.add(Restrictions.in(fieldName, (List<?>) fieldValue));
							} else if (fieldValue instanceof Number) {
								criteria.add(Restrictions.eq(fieldName, (fieldValue)));
							} else if (fieldValue instanceof Boolean) {
								criteria.add(Restrictions.eq(fieldName, (fieldValue)));
							} else if (fieldValue == null){
								criteria.add(Restrictions.isNull(fieldName));
							}
							break;
					}
					break;
				case "java.util.Date":
					if (fieldValue instanceof String) {
						String fieldValue2 = ((String) fieldValue).trim();
						Pattern.compile("\\s{2}").matcher(fieldValue2).replaceAll("\\s");
						Pattern.compile("\\s-\\s").matcher(fieldValue2).replaceAll("-");
						String[] times = fieldValue2.split("-", -1);
						SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
						SimpleDateFormat formatter1 = new SimpleDateFormat("dd/MM/yyyy HH:mm");
						SimpleDateFormat formatter2 = new SimpleDateFormat("dd/MM/yyyy HH");
						SimpleDateFormat formatter3 = new SimpleDateFormat("dd/MM/yyyy");
						Date startTime = null;
						Date endTime = null;
						if (times[0].trim() != "") {
							if (Pattern.compile("\\d{1,2}/\\d{1,2}/\\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}").matcher(times[0].trim()).matches())
								startTime = formatter.parse(times[0].trim());
							else if (Pattern.compile("\\d{1,2}/\\d{1,2}/\\d{4} \\d{1,2}:\\d{1,2}").matcher(times[0].trim()).matches())
								startTime = formatter1.parse(times[0].trim());
							else if (Pattern.compile("\\d{1,2}/\\d{1,2}/\\d{4} \\d{1,2}").matcher(times[0].trim()).matches())
								startTime = formatter2.parse(times[0].trim());
							else if (Pattern.compile("\\d{1,2}/\\d{1,2}/\\d{4}").matcher(times[0].trim()).matches())
								startTime = formatter3.parse(times[0].trim());
						}
						if (times.length >= 2) {
							if (times[1].trim() != "") {
								if (Pattern.compile("\\d{1,2}/\\d{1,2}/\\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}").matcher(times[1].trim()).matches())
									endTime = formatter.parse(times[1].trim());
								else if (Pattern.compile("\\d{1,2}/\\d{1,2}/\\d{4} \\d{1,2}:\\d{1,2}").matcher(times[1].trim()).matches())
									endTime = formatter1.parse(times[1].trim());
								else if (Pattern.compile("\\d{1,2}/\\d{1,2}/\\d{4} \\d{1,2}").matcher(times[1].trim()).matches())
									endTime = formatter2.parse(times[1].trim());
								else if (Pattern.compile("\\d{1,2}/\\d{1,2}/\\d{4}").matcher(times[1].trim()).matches())
									endTime = formatter3.parse(times[1].trim());
							}
						}
						fieldValue = new Date[] { startTime, endTime };
					}

					if (fieldValue instanceof List<?>) {
						if (((List<?>) fieldValue).size() > 0 && ((List<?>) fieldValue).get(0) instanceof Date) {
							fieldValue = ((List<?>) fieldValue).toArray(new Date[((List<?>) fieldValue).size()]);
						}
					}
					if (fieldValue instanceof Date[]) {
						Date startTime = null;
						Date endTime = null;
						if (((Date[]) fieldValue).length == 2) {
							if (((Date[]) fieldValue)[0] != null && ((Date[]) fieldValue)[0] instanceof Date) {
								startTime = ((Date[]) fieldValue)[0];
							}
							if (((Date[]) fieldValue)[1] != null && ((Date[]) fieldValue)[1] instanceof Date) {
								endTime = ((Date[]) fieldValue)[1];
							}

						}
						if (startTime != null && endTime != null) {
							criteria.add(Restrictions.between(fieldName, startTime, endTime));

						} else if (startTime != null && endTime == null) {
							if (expr != null && expr.equals(GT))
								criteria.add(Restrictions.gt(fieldName, startTime));
							else
								criteria.add(Restrictions.ge(fieldName, startTime));

						} else if (startTime == null && endTime != null) {
							if (expr != null && expr.equals(LT))
								criteria.add(Restrictions.lt(fieldName, endTime));
							else
								criteria.add(Restrictions.le(fieldName, endTime));
						}
					} else if (fieldValue instanceof Date) {
						switch (expr) {
							case LT:
								criteria.add(Restrictions.lt(fieldName, new Timestamp(((Date) fieldValue).getTime())));
								break;
							case GT:
								criteria.add(Restrictions.gt(fieldName, new Timestamp(((Date) fieldValue).getTime())));
								break;
							case LE:
								criteria.add(Restrictions.le(fieldName, new Timestamp(((Date) fieldValue).getTime())));
								break;
							case GE:
								criteria.add(Restrictions.ge(fieldName, new Timestamp(((Date) fieldValue).getTime())));
								break;
							case EXAC:
							default:
								criteria.add(Restrictions.eq(fieldName, new Timestamp(((Date) fieldValue).getTime())));
								break;
						}

					}
					break;
			}

		}

	}

	/**
	 * Thêm order cho criteria.
	 * 
	 * @param criteria
	 * 
	 * @return criteria
	 */
	protected Criteria setCriteriaOrders(Criteria criteria, Map<String, String> orders) {
		if (orders == null)
			return criteria;

//		final String _ASC = "ASC";
//		final String _DESC = "DESC";
		String propertyName;
		String orderType;
		for (Iterator<String> it = orders.keySet().iterator(); it.hasNext();) {
			propertyName = it.next();
			orderType = orders.get(propertyName);

			switch (orderType.toUpperCase()) {
			case "ASC":
				criteria.addOrder(Order.asc(propertyName));
				break;
			case "DESC":
				criteria.addOrder(Order.desc(propertyName));
				break;
			default:
				criteria.addOrder(Order.asc(propertyName));
				break;
			}
		}

		return criteria;
	}

	/**
	 * Get all field and type of object.
	 * 
	 * @return
	 * @throws ClassNotFoundException
	 */
	private Map<String, String> getFields() {
		PropertyDescriptor[] propertyDescriptors;
		Map<String, String> result = new HashMap<String, String>();
		try {
			propertyDescriptors = Introspector.getBeanInfo(domainClass).getPropertyDescriptors();
			String fieldName;
			String fieldType;
			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				fieldName = propertyDescriptor.getName();
				fieldType = propertyDescriptor.getPropertyType().getCanonicalName();

				if (ClassUtils.isPrimitiveOrWrapper(Class.forName(fieldType))) {
					result.put(fieldName, fieldType);
				} else if ("java.lang.String".equalsIgnoreCase(fieldType)) {
					result.put(fieldName, fieldType);
				} else if ("java.util.Date".equals(fieldType)) {
					result.put(fieldName, fieldType);
				} else {
					if (!"java.lang.Class".equals(fieldType)) {
						result = getSubField(fieldName, fieldType, result);
					}
				}
			}
		} catch (IntrospectionException | ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
		}

		return result;
	}

	private Map<String, String> getSubField(String fieldName, String fieldType, Map<String, String> result) {
		try {
			PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(Class.forName(fieldType))
					.getPropertyDescriptors();
			String subFieldName;
			String subFieldType;
			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				subFieldName = fieldName.concat("." + propertyDescriptor.getName());
				subFieldType = propertyDescriptor.getPropertyType().getCanonicalName();

			if (ClassUtils.isPrimitiveOrWrapper(propertyDescriptor.getPropertyType())) {
					result.put(subFieldName, subFieldType);
				} else if ("java.lang.String".equalsIgnoreCase(subFieldType)) {
					result.put(subFieldName, subFieldType);
				} else if ("java.util.Date".equalsIgnoreCase(subFieldType)) {
					result.put(subFieldName, subFieldType);
				} else {
					if (!"java.lang.Class".equals(subFieldType)) {
						result = getSubField(subFieldName, subFieldType, result);
					}
				}
			}
		} catch (IntrospectionException | ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
		}

		return result;
	}

	/**
	 * Get the actual type arguments a child class has used to extend a generic
	 * base class. (Taken from
	 * http://www.artima.com/weblogs/viewpost.jsp?thread=208860. Thanks
	 * mathieu.grenonville for finding this solution!)
	 * 
	 * @param baseClass
	 *            the base class
	 * @param childClass
	 *            the child class
	 * @return a list of the raw classes for the actual type arguments.
	 */
	public static <T> List<Class<?>> getTypeArguments(Class<T> baseClass, Class<? extends T> childClass) {
		Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
		Type type = childClass;
		// start walking up the inheritance hierarchy until we hit baseClass
		while (!getClass(type).equals(baseClass)) {
			if (type instanceof Class) {
				// there is no useful information for us in raw types, so just
				// keep going.
				type = ((Class) type).getGenericSuperclass();
			} else {
				ParameterizedType parameterizedType = (ParameterizedType) type;
				Class<?> rawType = (Class) parameterizedType.getRawType();

				Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
				TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
				for (int i = 0; i < actualTypeArguments.length; i++) {
					resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);
				}

				if (!rawType.equals(baseClass)) {
					type = rawType.getGenericSuperclass();
				}
			}
		}

		// finally, for each actual type argument provided to baseClass,
		// determine (if possible)
		// the raw class for that type argument.
		Type[] actualTypeArguments;
		if (type instanceof Class) {
			actualTypeArguments = ((Class) type).getTypeParameters();
		} else {
			actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
		}
		List<Class<?>> typeArgumentsAsClasses = new ArrayList<Class<?>>();
		// resolve types by chasing down type variables.
		for (Type baseType : actualTypeArguments) {
			while (resolvedTypes.containsKey(baseType)) {
				baseType = resolvedTypes.get(baseType);
			}
			typeArgumentsAsClasses.add(getClass(baseType));
		}
		return typeArgumentsAsClasses;
	}

	/**
	 * Get the underlying class for a type, or null if the type is a variable
	 * type.
	 * 
	 * @param type
	 *            the type
	 * @return the underlying class
	 */
	private static Class<?> getClass(Type type) {
		if (type instanceof Class) {
			return (Class) type;
		} else if (type instanceof ParameterizedType) {
			return getClass(((ParameterizedType) type).getRawType());
		} else if (type instanceof GenericArrayType) {
			Type componentType = ((GenericArrayType) type).getGenericComponentType();
			Class<?> componentClass = getClass(componentType);
			if (componentClass != null) {
				return Array.newInstance(componentClass, 0).getClass();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
}
