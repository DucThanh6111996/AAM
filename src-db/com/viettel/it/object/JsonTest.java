package com.viettel.it.object;

import com.google.gson.Gson;
import org.omnifaces.util.Json;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanhnv68 on 7/17/2017.
 */
public class JsonTest {
    private String label;
    private List<Item> lstItem;

    public JsonTest(String label, List<Item> lstItem) {
        this.label = label;
        this.lstItem = lstItem;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Item> getLstItem() {
        return lstItem;
    }

    public void setLstItem(List<Item> lstItem) {
        this.lstItem = lstItem;
    }

//    public static void main(String args[]) {
//        try {
//            List<Item> lstItem = new ArrayList<>();
//            for(int i = 0; i < 10; i++) {
//                Item item = new Item(i+"", i+"");
//                lstItem.add(item);
//            }
//
//            JsonTest test = new JsonTest("test", lstItem);
//
//            String data = new Gson().toJson(test);
//
//            JsonTest result = new Gson().fromJson(data, JsonTest.class);
//            System.out.println(result.getLstItem().size());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
