package com.viettel.it.controller;

import com.viettel.it.persistence.CarService;
import com.viettel.it.persistence.CarService.Car;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name="dtSummaryRowView")
public class SummaryRowView implements Serializable {
     
    private List<Car> cars;
     
    @ManagedProperty("#{carService}")
    private CarService service;
 
    @PostConstruct
    public void init() {
        cars = service.createCars(50);
    }
     
    public List<Car> getCars() {
        return cars;
    }
 
    public void setService(CarService service) {
        this.service = service;
    }
     
    public int getRandomPrice() {
        return (int) (Math.random() * 100000);
    }
}