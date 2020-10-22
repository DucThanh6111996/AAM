package com.viettel.persistence;

// Created May 12, 2016 11:21:40 AM by quanns2

import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.viettel.controller.AomClientServiceImpl;
import com.viettel.controller.Module;
import com.viettel.bean.ModuleChecklist;
import com.viettel.controller.ServerChecklist;
import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.KpiServerSetting;
import com.viettel.controller.AamConstants;
import com.viettel.util.HibernateUtil;
import com.viettel.controller.TextUtils;
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
import java.util.*;

/**
 * Service implement for interface KpiServerSettingService.
 * @see KpiServerSettingService
 * @author quanns2
 */

@Service(value = "kpiServerSettingService")
@Scope("session")
public class KpiServerSettingServiceImpl extends GenericDaoImpl<KpiServerSetting, Serializable> implements KpiServerSettingService, Serializable {
	private static Logger logger = LogManager.getLogger(KpiServerSettingServiceImpl.class);

	@Override
	public List<KpiServerSetting> findSettingForModules(List<Long> moduleIds) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		List<KpiServerSetting> objects = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(domainClass);
			criteria.add(Restrictions.in("appId", moduleIds));
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
	public List<KpiServerSetting> findAomSettingForModules(List<Module> modules) throws AppException, SysException {
		AomClientService aomClientService = new AomClientServiceImpl();
		List<KpiServerSetting> kpiServerSettings = new ArrayList<>();

		Set<String> ipServers = new HashSet<>();
		List<Long> moduleIds = new ArrayList<>();
		for (Module module : modules) {
			ipServers.add(module.getIpServer());
			moduleIds.add(module.getModuleId());
		}

		List<ServerChecklist> serverChecklists = aomClientService.findModulesByIds(new ArrayList<>(ipServers));
		Multimap<String, ServerChecklist> serverChecklistDisks = HashMultimap.create();
		Map<Long, KpiServerSetting> kpiServerSettingDisks = new HashMap<>();

		for (ServerChecklist serverChecklist : serverChecklists) {
			KpiServerSetting kpiServerSetting = null;
			switch (serverChecklist.getServiceId().intValue()) {
				case AamConstants.AOM_CHECKLIST.CPU_SERVER:
					kpiServerSetting = new KpiServerSetting();
					kpiServerSetting.setKpiId(AamConstants.CHECKLIST_ID.CPU_SERVER);
					break;
				case AamConstants.AOM_CHECKLIST.RAM_SERVER:
					kpiServerSetting = new KpiServerSetting();
					kpiServerSetting.setKpiId(AamConstants.CHECKLIST_ID.RAM_SERVER);
					break;
				case AamConstants.AOM_CHECKLIST.DISK:
					kpiServerSetting = new KpiServerSetting();
					kpiServerSetting.setKpiId(AamConstants.CHECKLIST_ID.DISK);
					break;
				case AamConstants.AOM_CHECKLIST.IO:
					kpiServerSetting = new KpiServerSetting();
					kpiServerSetting.setKpiId(AamConstants.CHECKLIST_ID.IO);
					break;
				default:
					break;
			}
			if (kpiServerSetting != null) {
				try {
					kpiServerSetting.setMathOption(AamConstants.CKL_MATH_OPERATOR.OPT_LT);
//					String critical = serverChecklist.getChecklistAlarms().get(0).getCritical();
//					critical = critical == null ? serverChecklist.getChecklistAlarms().get(0).getWarning() : critical;
//					kpiServerSetting.setNumberDefaultValue(Float.valueOf(critical));
					kpiServerSetting.setNumberDefaultValue(TextUtils.getCriticalAlarm(serverChecklist.getTimeMonitor()));
					if (kpiServerSetting.getNumberDefaultValue() == null)
						continue;

					List<String> ips = Splitter.on("_").trimResults().omitEmptyStrings().splitToList(serverChecklist.getModuleName());


					for (Module module : modules) {
						if (module.getIpServer().equals(ips.get(0))) {
							KpiServerSetting kpiServer = new KpiServerSetting();
							kpiServer.setAppId(module.getModuleId());
							kpiServer.setKpiId(kpiServerSetting.getKpiId());
							kpiServer.setMathOption(kpiServerSetting.getMathOption());
							kpiServer.setNumberDefaultValue(kpiServerSetting.getNumberDefaultValue());

							if (serverChecklist.getServiceId().intValue() == AamConstants.AOM_CHECKLIST.DISK && ips.size() == 2) {

								serverChecklistDisks.put(ips.get(0), serverChecklist);
								kpiServer.setServerChecklists(new ArrayList<>(serverChecklistDisks.get(ips.get(0))));
//								KpiServerSetting kpiServerSettingTmp = kpiServerSettingDisks.get(module.getModuleId());
								/*if (kpiServerSettingTmp == null)
									kpiServerSettings.add(kpiServer);
								else
									kpiServer.setServerChecklists(new ArrayList<>(serverChecklistDisks.get(ips.get(1))));
*/
								kpiServerSettingDisks.put(module.getModuleId(), kpiServer);
							} else {
								kpiServerSettings.add(kpiServer);
							}
						}
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

		kpiServerSettings.addAll(kpiServerSettingDisks.values());

		List<ModuleChecklist> moduleChecklists = aomClientService.findChecklistModulesByIds(moduleIds);
		Multimap<Long, ModuleChecklist> moduleChecklistLogErrors = HashMultimap.create();
		for (ModuleChecklist moduleChecklist : moduleChecklists) {
			KpiServerSetting kpiServerSetting = null;
			switch (moduleChecklist.getServiceId().intValue()) {
				case AamConstants.AOM_CHECKLIST.CHECK_ERROR_LOG:
					kpiServerSetting = new KpiServerSetting();
					kpiServerSetting.setKpiId(AamConstants.CHECKLIST_ID.CHECK_ERROR_LOG);
					kpiServerSetting.setMathOption(AamConstants.CKL_MATH_OPERATOR.OPT_NOT_OR);
					moduleChecklistLogErrors.put(moduleChecklist.getProcessId(), moduleChecklist);

//					kpiServerSetting.setStringDefaultValue(moduleChecklist.getAdvance());
//					kpiServerSetting.setExceptionValue(moduleChecklist.getAdvance());
					/*String advance = moduleChecklist.getAdvance();
					List<LogError> logErrors = new ArrayList<>();
					if (StringUtils.isNotEmpty(advance)) {
						Pattern pattern = Pattern.compile("(.+)###(.*)###(\\d+)###(\\d+)");
						advance = advance.replace("@ERROR_CODE@:", "");
						List<String> lines = Splitter.on("\n").trimResults().omitEmptyStrings().splitToList(advance);
						for (String line : lines) {
							Matcher matcher = pattern.matcher(line);
							if (matcher.find()) {
								LogError logError = new LogError();
								logError.setErrorText(matcher.group(1));
								logError.setExcludeText(matcher.group(2));
								logError.setFrequency(Integer.valueOf(matcher.group(3)));
								logError.setClear(Integer.valueOf(matcher.group(4)));

								logErrors.add(logError);
							}
						}
					}

					kpiServerSetting.set*/
					break;
				case AamConstants.AOM_CHECKLIST.CPU_MODULE:
					kpiServerSetting = new KpiServerSetting();
					kpiServerSetting.setKpiId(AamConstants.CHECKLIST_ID.CPU_MODULE);
					kpiServerSetting.setMathOption(AamConstants.CKL_MATH_OPERATOR.OPT_LT);
					break;
				case AamConstants.AOM_CHECKLIST.RAM_MODULE:
					kpiServerSetting = new KpiServerSetting();
					kpiServerSetting.setKpiId(AamConstants.CHECKLIST_ID.RAM_MODULE);
					kpiServerSetting.setMathOption(AamConstants.CKL_MATH_OPERATOR.OPT_LT);
					break;
				default:
					break;
			}
			if (kpiServerSetting != null) {
				try {
//					String critical = moduleChecklist.getChecklistAlarms().get(0).getCritical();
//					kpiServerSetting.setNumberDefaultValue(Float.valueOf(critical));
					kpiServerSetting.setNumberDefaultValue(TextUtils.getCriticalAlarm(moduleChecklist.getTimeMonitor()));
					if (kpiServerSetting.getNumberDefaultValue() == null)
						continue;

					kpiServerSetting.setAppId(moduleChecklist.getProcessId());

					if (moduleChecklist.getServiceId().intValue() == AamConstants.AOM_CHECKLIST.CHECK_ERROR_LOG) {
						Collection<ModuleChecklist> checklists = moduleChecklistLogErrors.get(moduleChecklist.getProcessId());
						if (checklists != null && !checklists.isEmpty()) {
							kpiServerSetting.setModuleChecklists(new ArrayList<>(checklists));
						}
					}

					kpiServerSettings.add(kpiServerSetting);

					/*List<String> ips = Splitter.on("_").trimResults().omitEmptyStrings().splitToList(moduleChecklist.getModuleName());
					for (Module module : modules) {
						if (module.getIpServer().equals(ips.get(0))) {
							KpiServerSetting kpiServer = new KpiServerSetting();
							kpiServer.setAppId(module.getModuleId());
							kpiServer.setKpiId(kpiServerSetting.getKpiId());
							kpiServer.setMathOption(kpiServerSetting.getMathOption());
							kpiServer.setNumberDefaultValue(kpiServerSetting.getNumberDefaultValue());
							kpiServer.setNumberDefaultValue(kpiServerSetting.getNumberDefaultValue());
							kpiServerSettings.add(kpiServer);
						}
					}*/
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

		return kpiServerSettings;
	}
}
