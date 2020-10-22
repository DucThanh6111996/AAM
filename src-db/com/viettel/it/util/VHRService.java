package com.viettel.it.util;

import com.viettel.util.SessionWrapper;
import com.viettel.vhr.service.*;
import com.viettel.vsa.token.UserToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.ResourceBundle;

public class VHRService {


	static VHRWebService_PortType vhrWebServicePort;
	static String ipLan = null;
	static String ipWan =null;
	static String password;
	static String userName;
	protected static final Logger LOGGER = LoggerFactory.getLogger(VHRService.class);
	static VhrActor actor;
	static{
		//VHR
		try {
			
			ResourceBundle bundle = ResourceBundle.getBundle("config");
			VHRWebService_ServiceLocator vhrWebService_ServiceLocator = new VHRWebService_ServiceLocator();
			vhrWebService_ServiceLocator.setVHRWebServicePortEndpointAddress(bundle.getString("VHR_WS"));
			vhrWebServicePort = vhrWebService_ServiceLocator.getVHRWebServicePort();
			
			try {
				ipLan = bundle.getString("ACTOR_IP_LAN");
			} catch (Exception e1) {
				LOGGER.error(e1.getMessage(), e1);
			}
			try {
				ipWan = bundle.getString("ACTOR_IP_WAN");
			} catch (Exception e1) {
				LOGGER.error(e1.getMessage(), e1);
			}
			password = bundle.getString("ACTOR_PASS");
			userName = bundle.getString("ACTOR_USER");
			actor = new VhrActor(ipLan, ipWan, password, userName);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	public void getProvince(){
		try {
			
			UserToken vsaValidate = ((UserToken) SessionWrapper.getCurrentSession().getAttribute(SessionWrapper._VSA_USER_TOKEN));
			String employeeCode = vsaValidate.getStaffCode();
	//		employeeCode = "064572";
	//		employeeCode = "008303";
	//		employeeCode = "005897"; //KV1
			
			
			EmployeeBean employee = vhrWebServicePort.getEmployeeInfo(actor, employeeCode, new SimpleDateFormat("dd/MM/yyyy").format(new Date()))[0];
			//Province
			if(Arrays.asList(new String[]{"GD","PGD"}).contains(employee.getPositionType())){
				//System.err.println(employee.getPositionName());
//				Long provinceId = employee.getCurrentOrganizationId();
	//			{
	//				DepartmentVsa departmentVsa = new DepartmentVsaServiceImpl().findById(provinceId);
	//				Map<String, String> filter = new HashMap<>();
	//				filter.put("departmentVsaId", departmentVsa.getDepartmentId().toString());
	//				List<Province> provinces = new ProvinceServiceImpl().findListExac(filter, null);
	//				if(provinces.size()>0){
	//					Province province = provinces.get(0);
	//					//JSONObject area = new JSONObject().put("areaCode", province.getAreaCode()).put("areaName", "Khu vực "+province.getAreaCode().substring(2));
	//					Zone area = new Zone(Long.parseLong("7"+province.getAreaCode().substring(2)), province.getAreaCode(), "Khu vực "+province.getAreaCode().substring(2));
	//					session.setAttribute("area", area);
	//					session.setAttribute("province", province);
	//					    
	//				}
	//				filter.clear();
	//				filter.put("departmentVsaId", new DepartmentVsaServiceImpl().findById(employee.getOrganizationId()).getParentId().toString());
	//				List<District> dis = new DistrictDaoImpl().findListExac(filter, null);
	//				if(dis.size()>0){
	//					District district = dis.get(0);
	//					jResult.put("district", createJSON(district));
	//					session.setAttribute("district", district);
	//				}
	//			}
	//			if(mapRole.get(SessionUtil.TASK_MANAGER_VOD_AREA)!=null && mapRole.get(SessionUtil.TASK_MANAGER_VOD_AREA)){
	//				String areaCode = new DepartmentVsaServiceImpl().findAreaCode(employee.getOrganizationId());
	//				if(areaCode!=null) {
	//					//JSONObject area = new JSONObject().put("areaCode", areaCode).put("areaName", "Khu vực "+areaCode.substring(2));
	//					Zone area = new Zone(Long.parseLong("7"+areaCode.substring(2)), areaCode, "Khu vực "+areaCode.substring(2));
	//					jResult.put("area", createJSON(area));
	//					session.setAttribute("area", area);
	//				}
	//			}
				
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	public EmployeeBean getManageOfEmployee(){
		
		try {
			String employeeCode = "122215";//SessionUtil.getEmployeeCode();
			ServiceResponse employeeBean = vhrWebServicePort.getManageOfEmployee(actor, employeeCode, new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
			if(employeeBean.isIsSuccess()){
				EmployeeBean employee = (EmployeeBean)employeeBean.getResponse()[0];
				employee = vhrWebServicePort.getListEmployees(actor, employee.getEmployeeCode(),null, null)[0];
				return employee;
			}
		} catch (RemoteException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}
	public EmployeeBean[] getEmployeesOfDepartment(Long deptId){
		try {
			return vhrWebServicePort.getListEmployees(actor, null,deptId, null);
		} catch (RemoteException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}
	public static void main(String[] args) {
		new VHRService().getManageOfEmployee();
		
	}
}
