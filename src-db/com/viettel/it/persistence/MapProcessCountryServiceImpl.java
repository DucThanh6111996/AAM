package com.viettel.it.persistence;

import com.viettel.it.model.MapProcessCountry;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * Created by hienhv4 on 6/15/2017.
 */
@Scope("session")
@Service(value = "mapProcessCountryService")
public class MapProcessCountryServiceImpl extends GenericDaoImplNewV2<MapProcessCountry, Long> implements Serializable {
    private static final long serialVersionUID = -4109611148855610L;
}
