package com.viettel.it.util;

import com.google.common.collect.HashMultimap;
import com.viettel.bean.OsAccount;
import com.viettel.controller.IimClientService;
import com.viettel.controller.IimClientServiceImpl;
import com.viettel.it.model.FlowRunAction;
import com.viettel.it.model.Node;
import com.viettel.it.persistence.ActionDetailServiceImpl;
import com.viettel.it.persistence.NodeTypeServiceImpl;
import com.viettel.model.ActionCustomAction;
import com.viettel.model.ActionCustomGroup;
import com.viettel.model.ActionServer;
import com.viettel.model.ImpactProcess;
import com.viettel.persistence.ActionServiceImpl;
import com.viettel.persistence.ImpactProcessServiceImpl;
import com.viettel.controller.AamConstants;
import com.viettel.util.Constant;
import com.viettel.util.SessionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

public class ActionUtil {

	protected static final Logger logger = LoggerFactory.getLogger(ActionUtil.class);
	
	public static boolean checkExistActionDetail(Long vendorId, Long nodeTypeId,
			Long versionId, Long actionId) {
		boolean check = false;
		try {
			Map<String, Object> filters = new HashMap<String, Object>();
			filters.put("vendor.vendorId", vendorId);
			filters.put("nodeType.typeId", nodeTypeId);
			filters.put("action.actionId", actionId);
			filters.put("version.versionId", versionId);
			
			int count = new ActionDetailServiceImpl().count2(filters);
			if (count > 0) {
				check = true;
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			check = false;
		}
		return check;
	}
	public static boolean checkExistActionDetail2( Long nodeTypeId,
												 Long versionId, Long actionId) {
		boolean check = false;
		try {
			Map<String, Object> filters = new HashMap<String, Object>();
			//filters.put("vendor.vendorId", vendorId);
			filters.put("nodeType.typeId", nodeTypeId);
			filters.put("action.actionId", actionId);
			filters.put("version.versionId", versionId);

			int count = new ActionDetailServiceImpl().count2(filters);
			if (count > 0) {
				check = true;
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			check = false;
		}
		return check;
	}

	/**
	 * Create mob for case reboot database.
	 */
	public static com.viettel.model.Action createMobDb(List<Node> nodes, FlowRunAction flowRunAction) {
		try {
			String userName = "quytv7";
			String staffCode = "168695";
			String fullName = "";
			try {
				userName = SessionUtil.getCurrentUsername();
				fullName = SessionUtil.getFullName();
				staffCode = SessionUtil.getStaffCode();
			} catch(Exception e) {
				logger.error(e.getMessage(), e);
			}
			// START - Fill data for action
			com.viettel.model.Action action = new com.viettel.model.Action();
			Date date = new Date();
//			action.setCrNumber("TEST_VAS_"+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
			action.setCrState(0l);
			action.setRunStatus(AamConstants.RUN_STATUS.STAND_BY_STATUS);
			action.setCrNumber(Constant.CR_DEFAULT);
			action.setActionType(AamConstants.ACTION_TYPE.ACTION_TYPE_CR_NORMAL);
			action.setKbGroup(AamConstants.KB_GROUP.BD_UCTT);
			action.setKbType(AamConstants.KB_TYPE.BD_SERVER);
			// 1: reboot, 2: shutdown
			action.setActionRbSd(1l);
//			action.setCreatedBy("quytv7");
			action.setCreatedBy(userName);
			action.setCreatedTime(date);
			action.setReason(flowRunAction.getFlowRunName());
			action.setCrName(flowRunAction.getCrNumber());
			action.setFullName(fullName);
			action.setMaxConcurrent(3);
			action.setSourceDir(new SimpleDateFormat("yyyyMMddHHmmss").format(date));
//			String staffCode = "168695";
			action.setStaffCode(staffCode);
			action.setVerifyStatus(Constant.FINISH_FAIL_STATUS);
			action.setBeginTime(date);
			Date dt = new Date();
			Calendar c = Calendar.getInstance();
			c.setTime(dt);
			c.add(Calendar.DATE, 1);
			dt = c.getTime();
			action.setEndTime(dt);
			Set<ActionServer> hashSetActionServers = new HashSet<>();
			List<ActionServer> actionServers = new ArrayList<>();

			Map<String, Object> prFilters = new HashMap<>();
			// anhnt2 - 07/18/2018
			String nationCode = flowRunAction.getCountryCode() == null ? AamConstants.VNM : flowRunAction.getCountryCode().getCountryCode();
			prFilters.put("active", "1");
			prFilters.put("status", "1");
			prFilters.put("nationCode", nationCode);
			try {
				List<ImpactProcess> processes = new ImpactProcessServiceImpl().findList(prFilters, new HashMap<>());
				if (processes != null && processes.size() > 0) {
					action.setImpactProcess(processes.get(0));
				} else {
					return null;
				}

			} catch (com.viettel.exception.AppException e) {
				logger.error(e.getMessage(), e);
			}

			// Get list node ip
			// DU lieu gia xoa
			if (nodes != null) {
				for (int i = 0; i < nodes.size(); i++) {
					ActionServer actionServer = new ActionServer();
					actionServer.setIpServer(nodes.get(i).getNodeIp());

					// Set account has type monitor
					IimClientService iimClientService = new IimClientServiceImpl();
					List<OsAccount> osAccounts = iimClientService.findOsAccount(action.getImpactProcess().getNationCode(), nodes.get(i).getNodeIp());
					if (osAccounts != null) {
						for (OsAccount osAccount : osAccounts) {
							if (osAccount.getUserType().equals(2)) {
								actionServer.setMonitorAccount(osAccount.getUsername());
								continue;
							}
						}
					}
					hashSetActionServers.add(actionServer);
					actionServers.add(actionServer);
				}
			}
			action.setActionServers(hashSetActionServers);
			// END - Fill data for action

			// START - Fill data for custom groups
			ActionCustomGroup actionCustomGroup = new ActionCustomGroup();

			actionCustomGroup.setName("Service maintenance");
			actionCustomGroup.setAfterGroup(Constant.SUB_STEP_CLEARCACHE);
			actionCustomGroup.setRollbackAfter(Constant.ROLLBACK_STEP_CLEARCACHE);
			actionCustomGroup.setActionCustomActions(new HashSet<>());

			List<ActionCustomGroup> customGroups = Arrays.asList(actionCustomGroup);

			ActionCustomAction customAction = new ActionCustomAction();
			// For case reboot
			customAction.setType(4);
			customAction.setPriority(1);
			customAction.setWaitReason(MessageUtil.getResourceBundleMessage("mantemance.service"));
			actionCustomGroup.getActionCustomActions().add(customAction);
			// END - Fill data for custom groups

			// Save DB
			//20190416_tudn_start import rule config
//			new ActionServiceImpl().saveOrUpdateAction(action, new ArrayList<>(),
//					new ArrayList<>(), new ArrayList<>(), HashMultimap.create(), new ArrayList<>(),
//					new ArrayList<>(), customGroups,
//					null, null, actionServers, new ArrayList<>());
			new ActionServiceImpl().saveOrUpdateAction(action, new ArrayList<>(),
					new ArrayList<>(), new ArrayList<>(), HashMultimap.create(), new ArrayList<>(),
					new ArrayList<>(), customGroups,
					null, null, actionServers, new ArrayList<>(), new ArrayList<>());
			//20190416_tudn_end import rule config

			// Get new record
//			String userName = "quytv7";
			List<com.viettel.model.Action> lstAction = new ActionServiceImpl().findByUser(userName);
			if (lstAction != null && lstAction.size() > 0) {
				return lstAction.get(0);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
		return null;
	}

	public static void main(String[] args) {
		try {
			System.out.println((new NodeTypeServiceImpl().findById(-1L)).getTypeName());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
