package com.viettel.util;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by quan on 6/19/2016.
 */
public class TablesNamesFinderExt extends TablesNamesFinder {
    List<String> mySelectTableList = new ArrayList<>();
    boolean inSelect = true;
    /**
     * To solve JSqlParsers Problem in getting tablenames from subselect using an Insert
     * statement.
     *
     * @param insert
     * @return
     */
    @Override
    public List<String> getTableList(Insert insert) {
        List<String> list = super.getTableList(insert);
        if (insert.getSelect() != null) {
            insert.getSelect().getSelectBody().accept(this);
        }
        return list;
    }

    @Override
    public void visit(SubSelect subSelect) {
        inSelect = true;
        super.visit(subSelect);
    }

    @Override
    public void visit(Table tableName) {
        super.visit(tableName);
        if (inSelect && !mySelectTableList.contains(tableName.getFullyQualifiedName()))
            mySelectTableList.add(tableName.getFullyQualifiedName());
    }

    public List<String> getSelectTableList() {
        return mySelectTableList;
    }
}
