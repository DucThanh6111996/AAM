package com.viettel.it.webservice.object;

import com.viettel.it.util.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanhnv68 on 8/16/2017.
 */
public class ResetMopGroupDTO {
    private Integer resetType;
    private List<ResetMopDTO> resetMops = new ArrayList<>();

    public Integer getResetType() {
        return resetType;
    }

    public void setResetType(Integer resetType) {
        this.resetType = resetType;
    }

    public List<ResetMopDTO> getResetMops() {
        return resetMops;
    }

    public void setResetMops(List<ResetMopDTO> resetMops) {
        this.resetMops = resetMops;
    }

//    public static void main(String args[]) {
//        try {
//
//            System.out.println(PasswordEncoder.decrypt("iZIanMDsycs6anZ0kxN5KQ=="));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
