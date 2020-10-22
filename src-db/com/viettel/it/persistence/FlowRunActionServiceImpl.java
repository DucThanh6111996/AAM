/** Created on Tue Sep 06 09:17:09 ICT 2016
*
* Copyright (C) 2013 by Viettel Network Company. All rights reserved
*/
package com.viettel.it.persistence;

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.it.model.FlowActionResult;
import com.viettel.it.model.FlowRunAction;
import com.viettel.util.HibernateUtil;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.*;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.*;

/**
* FlowRunActionServiceImpl.java
*
* @author Huy, Nguyen Xuan<huynx6@viettel.com.vn>
* @since Tue Sep 06 09:17:09 ICT 2016
* @version 1.0.0
*/
@Scope("session")
@Service(value = "flowRunActionService")
public class FlowRunActionServiceImpl  extends GenericDaoImplNewV2<FlowRunAction, Long> implements GenericDaoServiceNewV2<FlowRunAction, Long>, Serializable{
	private static final long serialVersionUID = -4109611148855610L;

	public List<FlowActionResult> findResult(Long flowId) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		List<FlowActionResult> list = new ArrayList<>();
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();

//			String sql = "select\n" +
//					"  la.username, a.GROUP_ACTION_NAME as groupActionName, la.RESULT result, f.FLOW_RUN_NAME flowRunName, n.NODE_IP nodeIp, n.NODE_CODE nodeCode, aserver.name\n" +
//					"from FLOW_RUN_LOG_ACTION la\n" +
//					"  LEFT JOIN FLOW_RUN_ACTION f on la.FLOW_RUN_LOG_ID=f.FLOW_RUN_ID\n" +
//					"  LEFT JOIN NODE n on n.NODE_ID=la.NODE_ID\n" +
//					"  LEFT JOIN ACTION_OF_FLOW a on la.ACTION_OF_FLOW_ID=a.STEP_NUM\n" +
//					"  left join ACTION_DB_SERVER aserver on aserver.action_id=a.action_id\n" +
//					"where f.FLOW_RUN_ID=:flowId order by nodeIp, a.STEP_NUM";

			String sql = "select\n" +
					"  la.username, a.GROUP_ACTION_NAME as groupActionName, la.RESULT result, f.FLOW_RUN_NAME flowRunName, n.NODE_IP nodeIp, n.NODE_CODE nodeCode, aserver.name\n" +
					"from FLOW_RUN_LOG_ACTION la\n" +
					"  LEFT JOIN FLOW_RUN_ACTION f on la.FLOW_RUN_LOG_ID=f.FLOW_RUN_ID\n" +
					"  LEFT JOIN NODE n on n.NODE_ID=la.NODE_ID\n" +
					"  LEFT JOIN ACTION_OF_FLOW a on la.ACTION_OF_FLOW_ID=a.STEP_NUM\n" +
					"  left join ACTION_DB_SERVER aserver on aserver.action_id=a.action_id\n" +
					"where f.FLOW_RUN_ID=:flowId order by nodeIp, a.STEP_NUM";
			Query query = session.createSQLQuery(sql)
					.addScalar("result", StandardBasicTypes.INTEGER)
					.addScalar("groupActionName", StandardBasicTypes.STRING)
					.addScalar("username", StandardBasicTypes.STRING)
					.addScalar("nodeIp", StandardBasicTypes.STRING)
					.addScalar("nodeCode", StandardBasicTypes.STRING)
					.addScalar("name", StandardBasicTypes.STRING)
					.setParameter("flowId", flowId).setResultTransformer(Transformers.aliasToBean(FlowActionResult.class));

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
	
	public static void main(String[] args) {
		/*try {
			Map<String, Object> filters = new HashMap<>();
	         filters.put("createBy-" + GenericDaoImplNewV2.EXAC, "hunghq2");
	         filters.put("status", 0l);

	         LinkedHashMap<String, String> orders = new LinkedHashMap<>();
	         orders.put("createDate", "DESC");

	         List<FlowRunAction> flowRunActions = (new FlowRunActionServiceImpl()).findList(filters, orders);
	         System.out.println(flowRunActions.size());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}*/

		FlowRunActionServiceImpl flowRunActionService = new FlowRunActionServiceImpl();
		try {
			List<FlowActionResult> flowActionResults = flowRunActionService.findResult(13972L);
			Map<String, Map<String, Integer>> results = new HashMap<>();
			Set<String> actions = new LinkedHashSet<>();
			System.out.println(flowActionResults.size());
			for (FlowActionResult flowActionResult : flowActionResults) {
				actions.add(flowActionResult.getName() + " - " + flowActionResult.getGroupActionName());
				Map<String, Integer> serverResults = results.get(flowActionResult.getNodeIp());
				if (serverResults == null)
					serverResults = new HashMap<>();

				serverResults.put(flowActionResult.getName() + " - " + flowActionResult.getGroupActionName(), flowActionResult.getResult());

				results.put(flowActionResult.getNodeIp(), serverResults);
			}

			for (Map<String, Integer> stringIntegerMap : results.values()) {
				System.out.println(stringIntegerMap);
			}

			List<String> headers = new ArrayList<>(actions);



			String excelFileName = "H:/Test.xlsx";//name of excel file

			String sheetName = "Sheet1";//name of sheet

			XSSFWorkbook wb = new XSSFWorkbook();
			XSSFSheet sheet = wb.createSheet(sheetName) ;

			XSSFCellStyle headerStyle = wb.createCellStyle();
			headerStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(7, 128, 51)));
			headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			headerStyle.setWrapText(true);
//			headerStyle.set

//			headerStyle.setFont(font);

			XSSFRow row = sheet.createRow(0);
			XSSFCell cell = row.createCell(0);
			sheet.autoSizeColumn(0);
			sheet.setColumnWidth(0, 5000);
			cell.setCellStyle(headerStyle);
			cell.setCellValue("IP");

			cell = row.createCell(1);
			sheet.autoSizeColumn(1);
			sheet.setColumnWidth(1, 5000);
			cell.setCellStyle(headerStyle);
			cell.setCellValue("Final");

			for (int i = 0; i < headers.size(); i++) {
				cell = row.createCell(i + 2);
				sheet.autoSizeColumn(i + 2);
				sheet.setColumnWidth(i + 2, 5000);

				cell.setCellStyle(headerStyle);
				cell.setCellValue(headers.get(i));
			}

			int r = 0;
			for (Map.Entry<String, Map<String, Integer>> mapEntry : results.entrySet()) {
				row = sheet.createRow(++r);
				Map<String, Integer> serverResults = mapEntry.getValue();
				for (int i = 0; i < headers.size(); i++) {
					cell = row.createCell(0);
					cell.setCellValue(mapEntry.getKey());

					cell = row.createCell(1);
					cell.setCellValue("OK");
					for (Integer integer : serverResults.values()) {
						if (integer != null && integer == 0) {
							cell.setCellValue("NOK");
							break;
						}
					}

					cell = row.createCell(i + 2);
					Integer actionResult = serverResults.get(headers.get(i));
					cell.setCellValue(actionResult != null && actionResult == 1 ? "OK" : "NOK");
				}
			}

			FileOutputStream fileOut = new FileOutputStream(excelFileName);

			//write this workbook to an Outputstream.
			wb.write(fileOut);
			fileOut.flush();
			fileOut.close();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}