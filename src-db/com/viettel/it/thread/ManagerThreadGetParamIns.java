package com.viettel.it.thread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ManagerThreadGetParamIns {

    private static ManagerThreadGetParamIns instance;
    private Map<Long, AtomicInteger> mapSessionThreadStatus = new HashMap<Long, AtomicInteger>();

    private ManagerThreadGetParamIns() {

    }

    public static ManagerThreadGetParamIns getInstance() {
        if (instance == null) {
            synchronized (ManagerThreadGetParamIns.class) {
                if (instance == null) {
                    instance = new ManagerThreadGetParamIns();
                }
            }
        }
        return instance;
    }

    public Map<Long, AtomicInteger> getMapSessionThreadStatus() {
        return mapSessionThreadStatus;
    }

    public void setMapSessionThreadStatus(Map<Long, AtomicInteger> mapSessionThreadStatus) {
        this.mapSessionThreadStatus = mapSessionThreadStatus;
    }

//    public static void main(String args[]) {
//        try {
//			ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//			new FixedExecutionRunnable(new TestRunable(), 5, scheduler).runNTimes(10, TimeUnit.SECONDS);
//        } catch(Exception e){
//            e.printStackTrace();
//        }
//    }
}
