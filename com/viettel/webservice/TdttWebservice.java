package com.viettel.webservice;

import com.viettel.it.webservice.object.*;
import com.viettel.util.ProcedureDTO;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.util.List;

@WebService
//@SOAPBinding(style = Style.RPC)
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
public interface TdttWebservice {
	//20181119_tudn_start them danh sach lenh blacklist
	@WebMethod
	public abstract MopResult getListMop(@WebParam(name = "wsUser") String wsUser, @WebParam(name = "wsPass") String wsPass, @WebParam(name = "username") String username);
	//public abstract MopResult getListMop(@WebParam(name = "wsUser") String wsUser, @WebParam(name = "wsPass") String wsPass, @WebParam(name = "username") String username, @WebParam(name = "procedureId") String procedureId, @WebParam(name = "crType") String crType);
	//20181119_tudn_end them danh sach lenh blacklist

	//20190401_tudn_start them dau viec quy trinh cho GNOC
	public abstract MopResult getListMopForGNOC(@WebParam(name = "wsUser") String wsUser, @WebParam(name = "wsPass") String wsPass, @WebParam(name = "username") String username, @WebParam(name = "procedureDTO") ProcedureDTO procedureDTO);
	//20190401_tudn_end them dau viec quy trinh cho GNOC

	@WebMethod
	public abstract LinkCrResult linkCr(@WebParam(name = "wsUser") String wsUser, @WebParam(name = "wsPass") String wsPass, @WebParam(name = "crNumber") String crNumber, @WebParam(name = "mopCode") String mopCode, @WebParam(name = "username") String username, @WebParam(name = "crName") String crName, @WebParam(name = "startTime") String startTime, @WebParam(name = "endTime") String endTime, @WebParam(name = "crState") Long crState);

	@WebMethod
	public abstract MopFileResult getMopFile(@WebParam(name = "wsUser") String wsUser, @WebParam(name = "wsPass") String wsPass, @WebParam(name = "mopCode") String mopCode, @WebParam(name = "crNumber") String crNumber);

	@WebMethod
	public abstract AppGroupResult getListAppGroup(@WebParam(name = "wsUser") String wsUser, @WebParam(name = "wsPass") String wsPass);

	@WebMethod
	public abstract IpServiceResult getListIpGroup(@WebParam(name = "wsUser") String wsUser, @WebParam(name = "wsPass") String wsPass, @WebParam(name = "appGroupId") Long appGroupId);

	@WebMethod
	public abstract LinkCrResult updateRunAutoStatus(@WebParam(name = "wsUser") String wsUser, @WebParam(name = "wsPass") String wsPass, @WebParam(name = "crNumber") String crNumber, @WebParam(name = "mopCode") String mopCode, @WebParam(name = "runAuto") Boolean runAuto);
	
	@WebMethod
	//20190826_tudn_start lap lich tac dong tu dong GNOC
	public abstract LinkCrResult updateRunAutoStatusNew(@WebParam(name = "wsUser") String wsUser, @WebParam(name = "wsPass") String wsPass, @WebParam(name = "crNumber") String crNumber, @WebParam(name = "mopCode") String mopCode, @WebParam(name = "runAuto") Boolean runAuto,
													 @WebParam(name = "typeConfirmGNOC") Long typeConfirmGNOC, @WebParam(name = "typeRunGNOC") Long typeRunGNOC,
													 @WebParam(name = "crLinkGNOC") String crLinkGNOC);
	//20190826_tudn_end lap lich tac dong tu dong GNOC

	/*@WebMethod
	public abstract LinkCrResult sysWorklog(@WebParam(name = "wsUser") String wsUser,
											@WebParam(name = "wsPass") String wsPass,
											@WebParam(name = "mopCode") String mopCode,
											@WebParam(name = "worklogContent") String worklogContent,
											@WebParam(name = "worklogId") Long worklogId);*/

	//20190524_tudn_start tac dong toan trinh SR GNOC
	@WebMethod
	public abstract ResultGetListTemplatesByProcedure getListTemplatesByProcedure(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService, @WebParam(name = "countryCode") String countryCode,
																				  @WebParam(name = "procedureId") String procedureId, @WebParam(name = "workFlowId") String workFlowId);

	@WebMethod
	public abstract ResultDeleteDt deleteDts(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
											 @WebParam(name = "systemCreateId") String systemCreateId, @WebParam(name = "listDtDelete") List<DtObjDelete> listDtDelete);

	@WebMethod
	public abstract ResultCreateDtByFileInput createDtByFileInput(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
																  @WebParam(name = "countryCode") String countryCode, @WebParam(name = "systemCreateId") String systemCreateId,
																  @WebParam(name = "listDtDelete") List<DtObjDelete> listDtDelete,
																  @WebParam(name = "flowTemplatesObj") List<FlowTemplateGNOCObj> flowTemplatesObj);
	//20190524_tudn_start tac dong toan trinh SR GNOC

	//20190722_tudn_start lay thong tin MOP
	@WebMethod
	public abstract MopResult getMopInfo(@WebParam(name = "wsUser") String wsUser, @WebParam(name = "wsPass") String wsPass, @WebParam(name = "mopCode") String mopCode);
	//20190722_tudn_end lay thong tin MOP
}