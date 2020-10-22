package com.viettel.it.object;

/**
 * Created by hanhnv68 on 7/17/2017.
 */
public class Item {
    private String col1;
    private String col2;

    public Item(String col1, String col2) {
        this.col1 = col1;
        this.col2 = col2;
    }

    public String getCol1() {
        return col1;
    }

    public void setCol1(String col1) {
        this.col1 = col1;
    }

    public String getCol2() {
        return col2;
    }

    public void setCol2(String col2) {
        this.col2 = col2;
    }
}
