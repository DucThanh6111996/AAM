package com.viettel.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author quanns2
 */
@Entity
@Table(name = "TIME_ZONE")
public class TimeZone {
    private Long id;
    private String company;
    private String zone;
    //tuanda38_20180619_start
    private Double gmt;
    //tuanda38_20180619_end

    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public Double getGmt() {
        return gmt;
    }

    public void setGmt(Double gmt) {
        this.gmt = gmt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TimeZone timeZone = (TimeZone) o;

        return new EqualsBuilder()
                .append(id, timeZone.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .toHashCode();
    }

    @Override
//    public String toString() {
//        return String.format("%s - (GMT%s:00)%s", company, (gmt >= 0 ? "+" : "") + gmt, zone);
//    }
    //tuanda38_20180619_start
    public String toString() {
        int hour = gmt.intValue();
        int minute = (int) ((Math.abs(gmt) - Math.abs(gmt.intValue()))*60);
        return String.format("%s - (GMT%s:%s)%s", company, (gmt >= 0 ? "+" : "") + hour,minute, zone);
    }
    //tuanda38_20180619_end
}
