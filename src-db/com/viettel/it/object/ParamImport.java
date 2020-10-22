package com.viettel.it.object;

import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;

public class ParamImport implements DynaBean {
	BasicDynaClass s;
	@Override
	public boolean contains(String paramString1, String paramString2) {
		// TODO Auto-generated method stub
		return paramString1.equals(paramString2);
	}

	@Override
	public Object get(String paramString) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(String paramString, int paramInt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(String paramString1, String paramString2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DynaClass getDynaClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove(String paramString1, String paramString2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void set(String paramString, Object paramObject) {
		// TODO Auto-generated method stub

	}

	@Override
	public void set(String paramString, int paramInt, Object paramObject) {
		// TODO Auto-generated method stub

	}

	@Override
	public void set(String paramString1, String paramString2, Object paramObject) {
		// TODO Auto-generated method stub

	}

}
