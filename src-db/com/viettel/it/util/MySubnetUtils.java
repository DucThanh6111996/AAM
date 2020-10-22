package com.viettel.it.util;


import org.apache.commons.net.util.SubnetUtils;

import java.util.HashMap;
import java.util.Map;


public class MySubnetUtils {

	private SubnetUtils subnetUtils ;
	public MySubnetUtils(String cidrNotation) {
		if(cidrNotation.contains("/")){
			String[] split = cidrNotation.split("/", -1);
			if(IpUtil.isIpAddress(split[1])){
				subnetUtils = new SubnetUtils(split[0]+"/"+mapMark.get(split[1]));
				return;
			}
		}
		subnetUtils = new SubnetUtils(cidrNotation);
	}
	static Map<String,String> mapMark = new HashMap<>();
	static {
		for(int i=0;i<=32;i++){
			String a = new SubnetUtils("0.0.0.0/"+ i).getInfo().getNetmask();
			mapMark.put(a, i+"");
		}
		
	}

	public SubnetUtils getSubnetUtils() {
		return subnetUtils;
	}

	public void setSubnetUtils(SubnetUtils subnetUtils) {
		this.subnetUtils = subnetUtils;
	}
}
