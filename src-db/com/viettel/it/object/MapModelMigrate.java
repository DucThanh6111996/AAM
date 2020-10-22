package com.viettel.it.object;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import com.viettel.excel.util.ExcelParser;
import com.viettel.it.controller.ItCommandController;
import com.viettel.it.model.*;
import com.viettel.it.persistence.*;
import com.viettel.it.util.Config;
import org.apache.commons.collections.map.HashedMap;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Created by hanhnv68 on 7/26/2017.
 */
public class MapModelMigrate {

    public static Map<Integer, String> getMapActionMigrate() {
        Map<Integer, String> model = new HashMap<>();

        model.put(1, "actionName");
        model.put(2, "commands");

        return model;
    }

//    public static void main(String args[]) {
//        try {
//
//            File file = new File("E:\\Copy of list lenh OCS.xlsx");
//            InputStream inputStream = new FileInputStream(file);
//            Map<Integer, String> rows = new HashMap<Integer, String>();
//            Workbook workBook = null;
//
//            workBook = new XSSFWorkbook(inputStream);
//
//            Sheet sheet = workBook.getSheetAt(0);
//            ExcelParser<CmdMigreateModel> ex = new ExcelParser<CmdMigreateModel>(4);
//            rows = MapModelMigrate.getMapActionMigrate();
//            List<CmdMigreateModel> lstData = ex.getObjects(sheet, CmdMigreateModel.class, rows);
//
//            System.out.println(lstData.size());
//
//            Vendor vendor = new VendorServiceImpl().findById(3l);
//            NodeType nodeType = new NodeTypeServiceImpl().findById(100l);
//            Version version = new VersionServiceImpl().findById(99l);
//
//            // BSS
//            Action hlrBss = new ActionServiceImpl().findById(11423l);
//            Action ocsBss = new ActionServiceImpl().findById(10570l);
//            Action pcrfBss = new ActionServiceImpl().findById(11424l);
//            Action otherBss = new ActionServiceImpl().findById(11425l);
//
//            ActionDetail actionDetail;
//            for (CmdMigreateModel a : lstData) {
//                try {
//                    Action action = new Action();
//                    List<String> cmds = Arrays.asList(a.getCommands().trim().split("\n"));
//                    if (cmds != null && !cmds.isEmpty()) {
//                        if (cmds.get(0).trim().startsWith("HLR")) {
//                            action.setAction(hlrBss);
//                        } else if (cmds.get(0).trim().startsWith("IN")) {
//                            action.setAction(ocsBss);
//                        } else if (cmds.get(0).trim().startsWith("PCRF")) {
//                            action.setAction(pcrfBss);
//                        } else {
//                            action.setAction(otherBss);
//                        }
//                        action.setDescription(a.getActionName());
//                        action.setName(a.getActionName());
//
//                        Long actionId = new ActionServiceImpl().save(action);
//                        action.setActionId(actionId);
//
//                        actionDetail = new ActionDetail();
//                        actionDetail.setAction(action);
//                        actionDetail.setVendor(vendor);
//                        actionDetail.setNodeType(nodeType);
//                        actionDetail.setVersion(version);
//                        actionDetail.setIsActive(1l);
//                        actionDetail.setUserName("hunghq2");
//
//                        Long actionDetailId = new ActionDetailServiceImpl().save(actionDetail);
//                        actionDetail.setDetailId(actionDetailId);
//
//                        List<ActionCommand> actionCommands = new ArrayList<>();
//                        Map<String, Object> filters = new HashedMap();
//
//                        int i = 0;
//                        for (String cmd : cmds) {
//                            if (!cmd.trim().isEmpty()) {
//                                filters.clear();
//                                filters.put("commandName", cmd);
//                                CommandDetail cmdDetail = buildCmdDetails(cmd);
//                                if (cmdDetail != null) {
//                                    ActionCommand actionCommand = new ActionCommand();
//                                    actionCommand.setActionDetail(actionDetail);
//                                    actionCommand.setCreateTime(new Date());
//                                    actionCommand.setCommandDetail(cmdDetail);
//                                    actionCommand.setIsActive(1l);
//                                    actionCommand.setOrderRun(Long.valueOf(i));
//                                    actionCommand.setType(1l);
//                                    actionCommand.setUserName("hunghq2");
//
//                                    actionCommands.add(actionCommand);
//                                }
//                            }
//                            i++;
//                        }
//
//                        new ActionCommandServiceImpl().saveOrUpdate(actionCommands);
//
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    System.out.println(">>>>>>>>>> error: " + a.getActionName() + ":::" + a.getCommands());
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    public static CommandDetail buildCmdDetails(String cmds) {
//        List<CommandDetail> cmdDetails = new ArrayList<>();
//        if (cmds != null && !cmds.isEmpty()) {
//
//            try {
//                Map<String, Object> filters = new HashedMap();
//                CommandDetail commandDetail;
//                CommandTelnetParser cmdPaser;
//                Vendor vendor = new VendorServiceImpl().findById(3l);
//                NodeType nodeType = new NodeTypeServiceImpl().findById(100l);
//                Version version = new VersionServiceImpl().findById(99l);
//                List<ParamInput> paramInputs = new ArrayList<>();
//                filters.put("commandName", cmds.trim());
//                List<CommandDetail> cmdDetailsOld = new CommandDetailServiceImpl().findList(filters);
//                if (cmdDetailsOld != null && !cmdDetailsOld.isEmpty()) {
//                    cmdDetails.add(cmdDetailsOld.get(0));
//                } else {
//                    commandDetail = new CommandDetail();
//                    commandDetail.setCommandName(cmds.trim());
//                    commandDetail.setProtocol(Config.PROTOCOL_EXCHANGE);
//                    commandDetail.setCommandType(Config.COMMAND_TYPE.IMPACT.value);
//                    commandDetail.setCommandClassify(1l);
//                    commandDetail.setVendor(vendor);
//                    commandDetail.setVersion(version);
//                    commandDetail.setNodeType(nodeType);
//                    commandDetail.setUserName("hunghq2");
//                    commandDetail.setOperator("NO CHECK");
//                    commandDetail.setCreateTime(new Date());
//                    commandDetail.setIsActive(1l);
//
//                    cmdPaser = new CommandTelnetParser();
//                    cmdPaser.setCmd(cmds.trim());
//                    cmdPaser.setCmdEnd("");
//                    Long cmdPaserId = new CommandTelnetParserServiceImpl().save(cmdPaser);
//                    cmdPaser.setTelnetParserId(cmdPaserId);
//
//                    commandDetail.setCommandTelnetParser(cmdPaser);
//                    Long cmdDetailId = new CommandDetailServiceImpl().save(commandDetail);
//                    commandDetail.setCommandDetailId(cmdDetailId);
//
//                    cmdDetails.add(commandDetail);
//
//                    List<String> params = ItCommandController.getLstParam(cmds.trim());
//                    if (!params.isEmpty()) {
//
//                        for (String param : params) {
//                            ParamInput paramInput = new ParamInput();
//                            paramInput.setCommandDetail(commandDetail);
//                            paramInput.setCreateTime(new Date());
//                            paramInput.setIsActive(1l);
//                            paramInput.setParamCode(param);
//                            paramInput.setUserName("hunghq2");
//                            paramInput.setParamType(0l);
//                            paramInput.setReadOnly(false);
//
//                            paramInputs.add(paramInput);
//
//                        }
//                    }
//                }
//
//                if (!paramInputs.isEmpty()) {
//                    new ParamInputServiceImpl().saveOrUpdate(paramInputs);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return cmdDetails.isEmpty() ? null : cmdDetails.get(0);
//    }
}
