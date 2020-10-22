/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.dao;

import com.viettel.controller.AamConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pham Manh Hung
 */
public class SqlTemplate {

    /**
     * @param tableName
     * @param Column
     * @return insert into TableName(column(1), column(2), column(3),...,column(n)) Values (?,?,?,...,?)
     */
    public static String insertTemplate(String tableName, List<String> Column) {
        if (StringUtils.isEmpty(tableName)) {
            return AamConstants.SQL_TEMPLATE.EMPTY;
        }
        if (null == Column || 0 == Column.size()) {
            return AamConstants.SQL_TEMPLATE.EMPTY;
        }
        StringBuilder insertTemplate = new StringBuilder();
        StringBuilder column = new StringBuilder();
        StringBuilder values = new StringBuilder();
        for (int i = 0; i < Column.size(); i++) {
            column.append(Column.get(i)).append(AamConstants.SQL_TEMPLATE.COMMA);
            values.append(AamConstants.SQL_TEMPLATE.QUESTION).append(AamConstants.SQL_TEMPLATE.COMMA);
        }
        String col = null;
        String val = null;
        if (column.length() > 1) {
            col = column.substring(0, column.length() - 1);
        }
        if (values.length() > 1) {
            val = values.substring(0, values.length() - 1);
        }
        insertTemplate.append("INSERT INTO ").append(tableName).append("(").append(col).append(")").append(" VALUES").append("(").append(val).append(")");
        return insertTemplate.toString();
    }

    public static String insertTemplate(String tableName, List<String> Column, String idFileName, String sequenceName) {
        if (StringUtils.isEmpty(tableName)) {
            return AamConstants.SQL_TEMPLATE.EMPTY;
        }
        if (null == Column || 0 == Column.size()) {
            return AamConstants.SQL_TEMPLATE.EMPTY;
        }
        StringBuilder insertTemplate = new StringBuilder();
        StringBuilder column = new StringBuilder();
        StringBuilder values = new StringBuilder();
        column.append(idFileName).append(AamConstants.SQL_TEMPLATE.COMMA);
        values.append(sequenceName).append(".nextval").append(AamConstants.SQL_TEMPLATE.COMMA);
        for (int i = 0; i < Column.size(); i++) {
            column.append(Column.get(i)).append(AamConstants.SQL_TEMPLATE.COMMA);
            values.append(AamConstants.SQL_TEMPLATE.QUESTION).append(AamConstants.SQL_TEMPLATE.COMMA);
        }
        String col = null;
        String val = null;
        if (column.length() > 1) {
            col = column.substring(0, column.length() - 1);
        }
        if (values.length() > 1) {
            val = values.substring(0, values.length() - 1);
        }
        insertTemplate.append("INSERT INTO ").append(tableName).append("(").append(col).append(")").append(" VALUES").append("(").append(val).append(")");
        return insertTemplate.toString();
    }

    public static void main(String arg[]) {
        List<String> Column = new ArrayList<String>();
        Column.add("FIELD1");
        System.out.println(insertTemplate("ABC", Column, "ID", "seq"));
    }

    /**
     * @param tableName
     * @param column
     * @param Condition condition la mot list danh sach cac dieu kieu gom kieu dieu kien va dieu kien
     *                  kieu dieu kien gom kieu sap xep orderby va condition
     * @return Select column(1), column(2), ..., column(n) from tableName where condition(1)=? and condition(1)=? and ... condition(m)=? order by condtion(x)
     */
    public static String selectTemplate(String tableName, List<String> column, List<SqlParam> Condition) {

        if (StringUtils.isEmpty(tableName)) {
            return AamConstants.SQL_TEMPLATE.EMPTY;
        }

        if (null == column || 0 == column.size()) {
            return AamConstants.SQL_TEMPLATE.EMPTY;
        }

        StringBuilder selectTemplate = new StringBuilder();
        StringBuilder tmpField = new StringBuilder();
        if (!StringUtils.equals(column.get(0), "*")) {
            for (int i = 0; i < column.size(); i++) {
                tmpField.append(column.get(i)).append(AamConstants.SQL_TEMPLATE.COMMA);
            }

            String field = null;
            if (tmpField.length() > 1) {
                field = tmpField.substring(0, tmpField.length() - 1);
            }
            selectTemplate.append("SELECT ").append(field).append(AamConstants.SQL_TEMPLATE.SPACE);
        } else {
            selectTemplate.append("SELECT *").append(AamConstants.SQL_TEMPLATE.SPACE);
        }


        selectTemplate.append(AamConstants.SQL_TEMPLATE.FROM).append(AamConstants.SQL_TEMPLATE.SPACE).append(tableName);
        if (null != Condition && !Condition.isEmpty()) {
            StringBuilder tmpCondition = new StringBuilder();
            StringBuilder tmpOrder = new StringBuilder();
            for (int i = 0; i < Condition.size(); i++) {
                SqlParam condition = Condition.get(i);
                if (Condition.get(i).getName().contains("?")) {
                    tmpCondition.append(AamConstants.SQL_TEMPLATE.SPACE).append(Condition.get(i).getName()).append(AamConstants.SQL_TEMPLATE.AND);
                } else if (StringUtils.equals(condition.getName(), AamConstants.SQL_TEMPLATE.ORDER)) {
                    tmpOrder.append(AamConstants.SQL_TEMPLATE.SPACE).append(AamConstants.SQL_TEMPLATE.ORDER).append(AamConstants.SQL_TEMPLATE.SPACE).append(condition.getObject());
                } else {
                    tmpCondition.append(AamConstants.SQL_TEMPLATE.SPACE).append(condition.getName()).append("=").append(AamConstants.SQL_TEMPLATE.QUESTION).append(AamConstants.SQL_TEMPLATE.SPACE).append(AamConstants.SQL_TEMPLATE.AND);
                }
            }

            if (tmpCondition.length() > 3) {
                selectTemplate.append(AamConstants.SQL_TEMPLATE.SPACE).append(AamConstants.SQL_TEMPLATE.WHERE).append(tmpCondition.substring(0, tmpCondition.length() - 3));
            }
            if (tmpOrder.length() > 0) {
                selectTemplate.append(tmpOrder.toString());
            }
        }

        return selectTemplate.toString();
    }

    /**
     * @param tableName
     * @param column
     * @param Condition
     * @return update tableName set column(1)=?, column(2)=?, column(3)=?,...,column(n)=? Where condition(1)=?, condition(2)=?,...,condition(m)=?
     */
    public static String updateTemplate(String tableName, List<String> column, List<String> Condition) {
        if (StringUtils.isEmpty(tableName)) {
            return AamConstants.SQL_TEMPLATE.EMPTY;
        }

        if (null == column || 0 == column.size()) {
            return AamConstants.SQL_TEMPLATE.EMPTY;
        }

        StringBuilder Column = new StringBuilder();
        for (int i = 0; i < column.size(); i++) {
            if (column.get(i).contains("=") && column.get(i).contains("?")) {
                Column.append(column.get(i)).append(AamConstants.SQL_TEMPLATE.COMMA);
            } else {
                Column.append(column.get(i)).append("=").append(AamConstants.SQL_TEMPLATE.QUESTION).append(AamConstants.SQL_TEMPLATE.COMMA);
            }
        }
        String col = null;
        if (Column.length() > 1) {
            col = Column.substring(0, Column.length() - 1);
        }

        String con = null;
        if (Condition != null && !Condition.isEmpty()) {
            StringBuilder condition = new StringBuilder();
            for (int i = 0; i < Condition.size(); i++) {
                if (Condition.get(i).contains("?")) {
                    condition.append(AamConstants.SQL_TEMPLATE.SPACE).append(Condition.get(i)).append(AamConstants.SQL_TEMPLATE.AND);
                } else {
                    condition.append(AamConstants.SQL_TEMPLATE.SPACE).append(Condition.get(i)).append("=").append(AamConstants.SQL_TEMPLATE.QUESTION).append(AamConstants.SQL_TEMPLATE.SPACE).append(AamConstants.SQL_TEMPLATE.AND);
                }
            }
            if (condition.length() > 3) {
                con = condition.substring(0, condition.length() - 3);
            }
        }
        StringBuilder updateTemplate = new StringBuilder();

        updateTemplate.append("UPDATE ").append(tableName).append(AamConstants.SQL_TEMPLATE.SPACE).append("SET ").append(col);

        if (!StringUtils.isEmpty(con)) {
            updateTemplate.append(AamConstants.SQL_TEMPLATE.SPACE).append(AamConstants.SQL_TEMPLATE.WHERE).append(con);
        }
        return updateTemplate.toString();
    }

    public static String deleteTemplate(String tableName, List<String> Condition) {
        if (StringUtils.isEmpty(tableName)) {
            return AamConstants.SQL_TEMPLATE.EMPTY;
        }

        String con = null;
        if (null != Condition && !Condition.isEmpty()) {
            StringBuilder condition = new StringBuilder();
            for (int i = 0; i < Condition.size(); i++) {
                if (Condition.get(i).contains(AamConstants.SQL_TEMPLATE.QUESTION)) {
                    condition.append(AamConstants.SQL_TEMPLATE.SPACE).append(Condition.get(i)).append(AamConstants.SQL_TEMPLATE.SPACE).append(AamConstants.SQL_TEMPLATE.AND);
                } else {
                    condition.append(AamConstants.SQL_TEMPLATE.SPACE).append(Condition.get(i)).append("=").append(AamConstants.SQL_TEMPLATE.QUESTION).append(AamConstants.SQL_TEMPLATE.SPACE).append(AamConstants.SQL_TEMPLATE.AND);
                }
            }
            if (condition.length() > 3) {
                con = condition.substring(0, condition.length() - 3);
            }
        }

        StringBuilder updateTemplate = new StringBuilder();

        updateTemplate.append("DELETE FROM ").append(tableName).append(AamConstants.SQL_TEMPLATE.SPACE).append("WHERE").append(con);
        return updateTemplate.toString();
    }

    /**
     * @param tableName
     * @param column
     * @param Condition condition la mot list danh sach cac dieu kieu gom kieu dieu kien va dieu kien
     *                  kieu dieu kien gom kieu sap xep orderby va condition
     * @return Select column(1), column(2), ..., column(n) from tableName where condition(1)=? and condition(1)=? and ... condition(m)=? order by condtion(x)
     */
    public static String selectTemplateTime(String tableName, List<String> column, List<SqlParam> Condition, String time) {

        if (StringUtils.isEmpty(tableName)) {
            return AamConstants.SQL_TEMPLATE.EMPTY;
        }

        if (null == column || 0 == column.size()) {
            return AamConstants.SQL_TEMPLATE.EMPTY;
        }

        StringBuilder selectTemplate = new StringBuilder();
        StringBuilder tmpField = new StringBuilder();
        if (!StringUtils.equals(column.get(0), "*")) {
            for (int i = 0; i < column.size(); i++) {
                tmpField.append(column.get(i)).append(AamConstants.SQL_TEMPLATE.COMMA);
            }

            String field = null;
            if (tmpField.length() > 1) {
                field = tmpField.substring(0, tmpField.length() - 1);
            }
            selectTemplate.append("SELECT ").append(field).append(AamConstants.SQL_TEMPLATE.SPACE);
        } else {
            selectTemplate.append("SELECT *").append(AamConstants.SQL_TEMPLATE.SPACE);
        }


        selectTemplate.append(AamConstants.SQL_TEMPLATE.FROM).append(AamConstants.SQL_TEMPLATE.SPACE).append(tableName);
        if (null != Condition && !Condition.isEmpty()) {
            StringBuilder tmpCondition = new StringBuilder();
            StringBuilder tmpOrder = new StringBuilder();
            for (int i = 0; i < Condition.size(); i++) {
                SqlParam condition = Condition.get(i);
                if (Condition.get(i).getName().contains("?")) {
                    tmpCondition.append(AamConstants.SQL_TEMPLATE.SPACE).append(Condition.get(i).getName()).append(AamConstants.SQL_TEMPLATE.AND);
                } else if (StringUtils.equals(condition.getName(), AamConstants.SQL_TEMPLATE.ORDER)) {
                    tmpOrder.append(AamConstants.SQL_TEMPLATE.SPACE).append(AamConstants.SQL_TEMPLATE.ORDER).append(AamConstants.SQL_TEMPLATE.SPACE).append(condition.getObject());
                } else {
                    tmpCondition.append(AamConstants.SQL_TEMPLATE.SPACE).append(condition.getName()).append("=").append(AamConstants.SQL_TEMPLATE.QUESTION).append(AamConstants.SQL_TEMPLATE.SPACE).append(AamConstants.SQL_TEMPLATE.AND);
                }
            }

            tmpCondition.append("DATA_TIME < system - ").append(time).append("/60/24");

            if (tmpCondition.length() > 3) {
                selectTemplate.append(AamConstants.SQL_TEMPLATE.SPACE).append(AamConstants.SQL_TEMPLATE.WHERE).append(tmpCondition.substring(0, tmpCondition.length() - 3));
            }
            if (tmpOrder.length() > 0) {
                selectTemplate.append(tmpOrder.toString());
            }
        }

        return selectTemplate.toString();
    }
}
