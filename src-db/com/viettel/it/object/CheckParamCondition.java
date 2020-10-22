package com.viettel.it.object;

import com.viettel.it.model.Node;
import com.viettel.it.model.ParamCondition;
import com.viettel.it.model.ParamValue;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by VTN-PTPM-NV56 on 4/8/2019.
 */
public class CheckParamCondition {
    protected static final Logger logger = LoggerFactory.getLogger(CheckParamCondition.class);
    private String nodeCode;
    private String paramCode;
    private String paramValue;
    private String result;
    private String condition;

    public CheckParamCondition() {
    }

    public CheckParamCondition(String nodeCode, String paramCode, String paramValue, String result, String condition) {
        this.nodeCode = nodeCode;
        this.paramCode = paramCode;
        this.paramValue = paramValue;
        this.result = result;
        this.condition = condition;
    }

    public String getNodeCode() {
        return nodeCode;
    }

    public void setNodeCode(String nodeCode) {
        this.nodeCode = nodeCode;
    }

    public String getParamCode() {
        return paramCode;
    }

    public void setParamCode(String paramCode) {
        this.paramCode = paramCode;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    /**
     * Ham check dieu kien tham so
     *
     * @param lstParamConByFlowTempIds danh sach dieu kien tham so
     * @param nodes                    danh sach node mang
     * @param mapCheckParamCondition   ket qua dung de xuat excel
     * @param isSplitParamValue        true: split paramValue va dua vao mapCheckParamCondition (Web), false: ko split paramValue va dua vao mapCheckParamCondition (Service)
     * @return true: param thoa man dieu kien, false: khong thoa man dieu kien
     */
    public Boolean checkParamCondition(List<ParamCondition> lstParamConByFlowTempIds, List<Node> nodes, Map<Node, List<ParamValue>> mapParamValues, LinkedHashMap<String, CheckParamCondition> mapCheckParamCondition, boolean isSplitParamValue) {
        try {
            mapCheckParamCondition.clear();

            Map<String, List<ParamCondition>> mapParamCode = null;
            if (lstParamConByFlowTempIds != null && lstParamConByFlowTempIds.size() > 0) {
                mapParamCode = new HashMap<>();
                for (ParamCondition condition : lstParamConByFlowTempIds) {
                    if (condition.getParamInput() != null && condition.getParamInput().getParamCode() != null) {
                        String keyConditionParamCode = condition.getParamInput().getParamCode().toLowerCase().trim();
                        if (!mapParamCode.containsKey(keyConditionParamCode)) {
                            List<ParamCondition> lstParamConditions = new ArrayList<>();
                            lstParamConditions.add(condition);
                            mapParamCode.put(keyConditionParamCode, lstParamConditions);
                        } else {
                            mapParamCode.get(keyConditionParamCode).add(condition);
                        }

                    }
                }
            }

            Boolean checkResult = true;
            for (Node n : nodes) {
                List<ParamValue> paramValues = mapParamValues.get(n);
                if (paramValues != null) {
                    for (ParamValue pv : paramValues) {
                        if (pv.getParamValue() == null) {
                            pv.setParamValue("");
                        }
                        String keyParamCodePV = pv.getParamCode().toLowerCase().trim();
                        if (mapParamCode == null || mapParamCode.isEmpty() || !mapParamCode.containsKey(keyParamCodePV)) {
                            if (isSplitParamValue) {
                                String[] splParamValues = pv.getParamValue().split(";");
                                for (int i = 0; i < splParamValues.length; i++) {
                                    if (!mapCheckParamCondition.containsKey(n.getNodeCode() + "#" + keyParamCodePV + "#" + splParamValues[i])) {
                                        CheckParamCondition checkParamCondition = new CheckParamCondition();
                                        checkParamCondition.setNodeCode(n.getNodeCode());
                                        checkParamCondition.setParamCode(keyParamCodePV);
                                        checkParamCondition.setParamValue(splParamValues[i]);
                                        checkParamCondition.setCondition("");
                                        checkParamCondition.setResult("OK");
                                        mapCheckParamCondition.put(n.getNodeCode() + "#" + keyParamCodePV + "#" + splParamValues[i], checkParamCondition);
                                    }
                                }
                            } else {
                                if (!mapCheckParamCondition.containsKey(n.getNodeCode() + "#" + keyParamCodePV + "#" + pv.getParamValue())) {
                                    CheckParamCondition checkParamCondition = new CheckParamCondition();
                                    checkParamCondition.setNodeCode(n.getNodeCode());
                                    checkParamCondition.setParamCode(keyParamCodePV);
                                    checkParamCondition.setParamValue(pv.getParamValue());
                                    checkParamCondition.setCondition("");
                                    checkParamCondition.setResult("OK");
                                    mapCheckParamCondition.put(n.getNodeCode() + "#" + keyParamCodePV + "#" + pv.getParamValue(), checkParamCondition);
                                }
                            }
                        } else if (mapParamCode.containsKey(keyParamCodePV)) {
                            List<ParamCondition> paramConditions = mapParamCode.get(keyParamCodePV);

                            if (isSplitParamValue) {
                                String[] splParamValues = pv.getParamValue().split(";");
                                for (int i = 0; i < splParamValues.length; i++) {
                                    for (ParamCondition paramCondition : paramConditions) {
                                        String conditionValue = " " + paramCondition.getConditionValue();
                                        if (paramCondition.getConditionOperator().equalsIgnoreCase("no check")
                                                || paramCondition.getConditionOperator().equalsIgnoreCase("is null")
                                                || paramCondition.getConditionOperator().equalsIgnoreCase("not null")) {
                                            conditionValue = "";
                                        }
                                        if (mapCheckParamCondition.containsKey(n.getNodeCode() + "#" + keyParamCodePV + "#" + splParamValues[i])) {
                                            String conditions = mapCheckParamCondition.get(n.getNodeCode() + "#" + keyParamCodePV + "#" + splParamValues[i]).getCondition();
                                            CheckParamCondition cpc = mapCheckParamCondition.get(n.getNodeCode() + "#" + keyParamCodePV + "#" + splParamValues[i]);
                                            if (!conditions.contains(" AND ")) {
                                                if (!conditions.contains(paramCondition.getConditionOperator() + conditionValue)) {
                                                    conditions = conditions + " AND " + paramCondition.getConditionOperator() + conditionValue;
                                                }
                                            } else {
                                                if (!conditions.contains(" AND " + paramCondition.getConditionOperator() + conditionValue)) {
                                                    conditions = conditions + " AND " + paramCondition.getConditionOperator() + conditionValue;
                                                }
                                            }

                                            mapCheckParamCondition.get(n.getNodeCode() + "#" + keyParamCodePV + "#" + splParamValues[i]).setCondition(conditions);
                                            boolean checkValueOperator = checkValueOperator(splParamValues[i], paramCondition.getConditionValue(), paramCondition.getConditionOperator());
                                            if (!checkValueOperator) {
                                                mapCheckParamCondition.get(n.getNodeCode() + "#" + keyParamCodePV + "#" + splParamValues[i]).setResult("NOK");
                                                checkResult = false;
                                            }

                                        } else {
                                            CheckParamCondition checkParamCondition = new CheckParamCondition();
                                            checkParamCondition.setNodeCode(n.getNodeCode());
                                            checkParamCondition.setParamCode(keyParamCodePV);
                                            checkParamCondition.setParamValue(splParamValues[i]);
                                            String condition = paramCondition.getConditionOperator() + conditionValue;
                                            checkParamCondition.setCondition(condition);
                                            boolean checkValueOperator = checkValueOperator(splParamValues[i], paramCondition.getConditionValue(), paramCondition.getConditionOperator());
                                            if (checkValueOperator) {
                                                checkParamCondition.setResult("OK");
                                            } else {
                                                checkParamCondition.setResult("NOK");
                                                checkResult = false;
                                            }
                                            mapCheckParamCondition.put(n.getNodeCode() + "#" + keyParamCodePV + "#" + splParamValues[i], checkParamCondition);
                                        }
                                    }
                                }
                            } else {
                                for (ParamCondition paramCondition : paramConditions) {
                                    String conditionValue = " " + paramCondition.getConditionValue();
                                    if (paramCondition.getConditionOperator().equalsIgnoreCase("no check")
                                            || paramCondition.getConditionOperator().equalsIgnoreCase("is null")
                                            || paramCondition.getConditionOperator().equalsIgnoreCase("not null")) {
                                        conditionValue = "";
                                    }
                                    if (mapCheckParamCondition.containsKey(n.getNodeCode() + "#" + keyParamCodePV + "#" + pv.getParamValue())) {
                                        String conditions = mapCheckParamCondition.get(n.getNodeCode() + "#" + keyParamCodePV + "#" + pv.getParamValue()).getCondition()
                                                + " AND " + paramCondition.getConditionOperator() + conditionValue;
                                        mapCheckParamCondition.get(n.getNodeCode() + "#" + keyParamCodePV + "#" + pv.getParamValue()).setCondition(conditions);
                                        boolean checkValueOperator = checkValueOperator(pv.getParamValue(), paramCondition.getConditionValue(), paramCondition.getConditionOperator());
                                        if (!checkValueOperator) {
                                            mapCheckParamCondition.get(n.getNodeCode() + "#" + keyParamCodePV + "#" + pv.getParamValue()).setResult("NOK");
                                            checkResult = false;
                                        }
                                    } else {
                                        CheckParamCondition checkParamCondition = new CheckParamCondition();
                                        checkParamCondition.setNodeCode(n.getNodeCode());
                                        checkParamCondition.setParamCode(keyParamCodePV);
                                        checkParamCondition.setParamValue(pv.getParamValue());
                                        String condition = paramCondition.getConditionOperator() + conditionValue;
                                        checkParamCondition.setCondition(condition);
                                        boolean checkValueOperator = checkValueOperator(pv.getParamValue(), paramCondition.getConditionValue(), paramCondition.getConditionOperator());
                                        if (checkValueOperator) {
                                            checkParamCondition.setResult("OK");
                                        } else {
                                            checkParamCondition.setResult("NOK");
                                            checkResult = false;
                                        }
                                        mapCheckParamCondition.put(n.getNodeCode() + "#" + keyParamCodePV + "#" + pv.getParamValue(), checkParamCondition);
                                    }
                                }
                            }

                        }
                    }
                }
            }
            return checkResult;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return true;
    }

    //12-10-2018 TheNV end
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    // 20181113_thenv_start
    public static boolean checkValueOperator(String value, String compeValue, String comOper) {
        // value : gia tri nhap vao
        // compeValue: gia tri so sanh
        // comOper : toan tu so sanh
        boolean flag;
        String[] vals = null;
        String[] strArr = null;
        int i = 0;
        boolean checkSplit = true;
        if (!isNullOrEmpty(value)) {
            vals = value.split(";");
            checkSplit = vals.length == (org.apache.commons.lang3.StringUtils.countMatches(value, ";") + 1);
        }
        boolean checkVals = checkSplit && vals != null && vals.length > 0;
        switch (comOper.toLowerCase()) {
            case "no check":
                return true;
            case ">":
                if (!checkVals || compeValue == null) {
                    return false;
                }
                for (String val : vals) {
                    val = val.trim();
                    if (!NumberUtils.isNumber(val)) {
                        return false;
                    }
                    if (NumberUtils.isNumber(val) && NumberUtils.isNumber(compeValue)) {
                        flag = Double.parseDouble(val) > Double.parseDouble(compeValue);
                    } else {
                        flag = val.compareTo(compeValue) > 0;
                    }
                    if (!flag) {
                        return false;
                    }
                }
                return true;
            case ">=":
                if (!checkVals || compeValue == null) {
                    return false;
                }
                for (String val : vals) {
                    val = val.trim();
                    if (!NumberUtils.isNumber(val)) {
                        return false;
                    }
                    if (NumberUtils.isNumber(val) && NumberUtils.isNumber(compeValue)) {
                        flag = Double.parseDouble(val) >= Double.parseDouble(compeValue);
                    } else {
                        flag = val.compareTo(compeValue) >= 0;
                    }
                    if (!flag) {
                        return false;
                    }
                }
                return true;
            case "<":
                if (!checkVals || compeValue == null) {
                    return false;
                }
                for (String val : vals) {
                    val = val.trim();
                    if (!NumberUtils.isNumber(val)) {
                        return false;
                    }
                    if (NumberUtils.isNumber(val) && NumberUtils.isNumber(compeValue)) {
                        flag = Double.parseDouble(val) < Double.parseDouble(compeValue);
                    } else {
                        flag = val.compareTo(compeValue) < 0;
                    }
                    if (!flag) {
                        return false;
                    }
                }
                return true;
            case "<=":
                if (!checkVals || compeValue == null) {
                    return false;
                }
                for (String val : vals) {
                    val = val.trim();
                    if (!NumberUtils.isNumber(val)) {
                        return false;
                    }
                    if (NumberUtils.isNumber(val) && NumberUtils.isNumber(compeValue)) {
                        flag = Double.parseDouble(val) <= Double.parseDouble(compeValue);
                    } else {
                        flag = val.compareTo(compeValue) <= 0;
                    }
                    if (!flag) {
                        return false;
                    }
                }
                return true;
            case "=":
                if (!checkVals || compeValue == null) {
                    return false;
                }
                for (String val : vals) {
                    val = val.trim();
                    if (!val.equals(compeValue)) {
                        return false;
                    }
                }
                return true;
            case "<>":
                if (!checkVals) {
                    return false;
                }
                for (String val : vals) {
                    val = val.trim();
                    if (val.equals(compeValue)) {
                        return false;
                    }
                }
                return true;
            case "is null":
                if (vals == null || vals.length == 0) return true;
                for (String val : vals) {
                    if (!isNullOrEmpty(val)) return false;
                }
                return true;
            case "not null":
                if (!checkVals) return false;
                for (String val : vals) {
                    if (isNullOrEmpty(val)) return false;
                }
                return true;
            case "in":
                if (!checkVals || compeValue == null) {
                    return false;
                }
                strArr = compeValue.split(",");
                if (strArr.length == 0) {
                    return false;
                }
                i = 0;
                for (String val : vals) {
                    val = val.trim();
                    for (String str : strArr) {
                        if (val.equals(str)) {
                            i++;
                            break;
                        }
                    }
                }
                return i == vals.length;
            case "between":
                if (!checkVals || compeValue == null) {
                    return false;
                }
                strArr = compeValue.split(",");
                if (strArr.length == 0) {
                    return false;
                }

                for (String val : vals) {
                    val = val.trim();
                    if (strArr != null && strArr.length > 1) {
                        String below = strArr[0];
                        String behind = strArr[1];

                        if (NumberUtils.isNumber(val) && NumberUtils.isNumber(below) && NumberUtils.isNumber(behind)) {
                            if (!(Double.parseDouble(val) <= Double.parseDouble(behind) && Double.parseDouble(val) >= Double.parseDouble(below))) {
                                return false;
                            }
                        } else {
                            if (!(val.compareTo(behind) <= 0 && val.compareTo(below) >= 0)) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            case "is null or contain":
                if (!checkVals) {
                    return true;
                } else {
                    if (compeValue != null) {
                        strArr = compeValue.split(",");
                        if (strArr.length == 0) {
                            return false;
                        }

                        for (String str : strArr) {
                            for (String val : vals) {
                                if (val != null && val.contains(str)) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    } else {
                        return false;
                    }
                }
            case "contain":
                if (!checkVals || compeValue == null) {
                    return false;
                }
                strArr = compeValue.split(",");
                if (strArr.length == 0) {
                    return false;
                }

                if (compeValue != null) {
                    for (String str : strArr) {
                        for (String val : vals) {
                            if (val != null && val.contains(str)) {
                                return true;
                            }
                        }
                    }
                    return false;
                } else {
                    return false;
                }
            case "contain all":
                if (!checkVals || compeValue == null) {
                    return false;
                }
                strArr = compeValue.split(",");
                if (strArr.length == 0) {
                    return false;
                }

                i = 0;
                for (String str : strArr) {
                    for (String val : vals) {
                        if (val != null && val.contains(str)) {
                            i++;
                            break;
                        }
                    }
                }
                if (i == strArr.length) {
                    return true;
                } else {
                    return false;
                }
            case "not contain":
                if (!checkVals || compeValue == null) {
                    return false;
                }
                strArr = compeValue.split(",");
                if (strArr.length == 0) {
                    return false;
                }

                for (String str : strArr) {
                    for (String val : vals) {
                        if (val == null || val.contains(str)) {
                            return false;
                        }
                    }
                }
                return true;
            case "like":
                if (!checkVals || compeValue == null) {
                    return false;
                }
                for (String val : vals) {
                    if (val == null || !like(val, compeValue)) {
                        return false;
                    }
                }
                return true;
            case "not like":
                if (!checkVals || compeValue == null) {
                    return false;
                }
                for (String val : vals) {
                    if (val == null || like(val, compeValue)) {
                        return false;
                    }
                }
                return true;
            case "not in":
                if (vals == null || compeValue == null) {
                    return false;
                }
                strArr = compeValue.split(",");
                if (strArr.length == 0) {
                    return false;
                }

                for (String str : strArr) {
                    for (String val : vals) {
                        val = val.trim();
                        if (val.equals(str)) {
                            return false;
                        }
                    }
                }
                return true;
        }

        return false;
    }

    private static boolean like(String str, String expr) {
        try {
            expr = expr.toLowerCase(); // ignoring locale for now
            expr = expr.replace(".", "\\."); // "\\" is escaped to "\"
            // ... escape any other potentially problematic characters here
            expr = expr.replace("?", ".");
            expr = expr.replace("%", ".*");
            str = str.toLowerCase();
            return str.matches(expr);
        } catch (Exception ex) {
            logger.error("Exception like: " + ex.getMessage());
            return false;
        }
    }
}
