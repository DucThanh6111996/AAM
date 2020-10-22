package com.viettel.util;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.rits.cloning.Cloner;
import com.viettel.gnoc.cr.service.CrImpactInfoForGateProService;
import com.viettel.gnoc.cr.service.CrImpactInfoForGateProServiceImplServiceLocator;
import com.viettel.gnoc.cr.service.GnocInfoForGateProDTO;
import com.viettel.gnoc.cr.service.GnocInfoForGateProOutputDTO;
import com.viettel.vsa.token.UserToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.model.SelectItem;
import java.text.SimpleDateFormat;
import java.util.*;

public class CrUtil extends SessionWrapper {
	private static Logger logger = LogManager.getLogger(SessionWrapper.class);

	public static List<GnocInfoForGateProDTO> getListGnocInfo(String user, String crNumber, String exchangeIp) {
		Long timeZone = Long.valueOf(ResourceBundle.getBundle("config").getString("VssTimeZone"));
		Date dateTmp = new Date();
		Date date = new Date(dateTmp.getTime() + timeZone * 3600 * 1000);
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String dateStr = formatter.format(date);
		List<GnocInfoForGateProDTO> lst = new ArrayList<>();
		try {
			try {
				if (user == null) {
					UserToken vsaUserToken = (UserToken) getCurrentSession().getAttribute("vsaUserToken");
					if (vsaUserToken != null) {
						user = vsaUserToken.getUserName();
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			CrImpactInfoForGateProServiceImplServiceLocator crImpactInfoForGateProService = new CrImpactInfoForGateProServiceImplServiceLocator();
			CrImpactInfoForGateProService crService = crImpactInfoForGateProService.getCrImpactInfoForGateProServiceImplPort();
			GnocInfoForGateProOutputDTO infor = crService.getCrInfoByUserForGateProWithIp(null, null, null, null, dateStr, null, user, crNumber, null, null,
					exchangeIp);

			if (infor.getCrInfoForGateProOutput() != null)
				lst = Arrays.asList(infor.getCrInfoForGateProOutput());

		} catch (Exception e) {
			logger.debug(e.getMessage(), e);
			logger.error("Không lấy được CR " + e.getMessage());
		}

		return lst;
	}

	public List<GnocInfoForGateProDTO> getListCrForToolSync() {
		return getListGnocInfo(null, null, null);
	}

	Map<String, List<String>> mapNodeNoCr = new HashMap<String, List<String>>();

	public List<String> getCrWithListNode(List<String> nodeNames) {
		List<String> crs = new ArrayList<String>();
		List<GnocInfoForGateProDTO> gateProDTOs = getListCrForToolSync();
		Multimap<String, String> mapCrWithNode = LinkedListMultimap.create();
		for (GnocInfoForGateProDTO taskNightInfoForGatePro : gateProDTOs) {
			mapCrWithNode.put(taskNightInfoForGatePro.getCrNumber(), taskNightInfoForGatePro.getNodePortName());
		}
		for (Iterator<String> iterator = mapCrWithNode.keySet().iterator(); iterator.hasNext();) {
			String crNumber = (String) iterator.next();
			Collection<String> nodeInCrs = mapCrWithNode.get(crNumber);
			if (nodeInCrs != null && nodeInCrs.containsAll(nodeNames))
				crs.add(crNumber);
			else {
				List<String> clone = new Cloner().deepClone(nodeNames);
				clone.removeAll(nodeInCrs);
				mapNodeNoCr.put(crNumber, clone);
			}

		}
		return crs;
	}

	public static List<SelectItem> getListCrNumber() {
		List<SelectItem> listCr = new ArrayList<>();

		List<GnocInfoForGateProDTO> list = getListGnocInfo(null, null, null);
		Set<String> crs = new HashSet<>();
		for (GnocInfoForGateProDTO gateProDTO : list) {
			crs.add(gateProDTO.getCrNumber());
		}
		for (String cr : crs) {
			listCr.add(new SelectItem(cr, cr));
		}

		return listCr;
	}

	public static List<String> getCrNumber(String user) {
		List<GnocInfoForGateProDTO> list = getListGnocInfo(user, null, null);
		Set<String> crs = new HashSet<>();
		for (GnocInfoForGateProDTO gateProDTO : list) {
			crs.add(gateProDTO.getCrNumber());
		}

		return new ArrayList<>(crs);
	}

	public static List<String> getListNodeFromCr(String selectedCr) {
		Set<String> crs = new HashSet<>();
		List<GnocInfoForGateProDTO> list = getListGnocInfo(null, null, null);
		for (GnocInfoForGateProDTO gateProDTO : list) {
			if (gateProDTO.getCrNumber().equalsIgnoreCase(selectedCr))
				crs.add(gateProDTO.getNodePortName());
		}
		return new ArrayList<>(crs);
	}

	public static List<String> getListIpFromCr(String selectedCr) {
		Set<String> crs = new HashSet<>();
		List<GnocInfoForGateProDTO> list = getListGnocInfo(null, null, null);
		for (GnocInfoForGateProDTO taskNightInfoForGatePro : list) {
			if (taskNightInfoForGatePro.getCrNumber().equalsIgnoreCase(selectedCr))
				crs.add(taskNightInfoForGatePro.getIp());
		}
		return new ArrayList<>(crs);
	}

	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		System.out.println(new CrUtil().getListGnocInfo(null, null, null).size());
		System.err.println(new CrUtil().getListCrNumber());
		System.err.println(new CrUtil().getListNodeFromCr("CR_NORMAL_MANGLOI_14188"));
		System.err.println(new CrUtil().getListIpFromCr("CR_NORMAL_MANGLOI_14188"));
	}

}
