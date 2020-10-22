package com.viettel.bean;

import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.Serializable;

/**
 * @author anhnt2 - Rikkeisoft
 */
public class ActiveRoute implements Serializable {
    private String networkDestination;
    private String netMask;
    private String gateWay;
    private String metric;

    public String getNetworkDestination() {
        return networkDestination;
    }

    public void setNetworkDestination(String networkDestination) {
        this.networkDestination = networkDestination;
    }

    public String getNetMask() {
        return netMask;
    }

    public void setNetMask(String netMask) {
        this.netMask = netMask;
    }

    public String getGateWay() {
        return gateWay;
    }

    public void setGateWay(String gateWay) {
        this.gateWay = gateWay;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ActiveRoute active = (ActiveRoute) o;

        return new EqualsBuilder()
                .append(networkDestination, active.networkDestination)
                .isEquals();
    }
}
