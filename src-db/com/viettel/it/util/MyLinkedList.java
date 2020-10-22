package com.viettel.it.util;

import java.util.LinkedList;

public class MyLinkedList<E> extends LinkedList<E> {

	private static final long serialVersionUID = 6558244544203611953L;
	private String delimter = "";
	@Override
	public String toString() {
		String result = "";
        for (int i = 0; i < this.size(); i++) {
            result += this.get(i).toString()+delimter+"\r\n";
        }
        return result.replaceAll("\n$", "");
	}

	public static void main(String[] args) {
		
	}

	public String getDelimter() {
		return delimter;
	}

	public void setDelimter(String delimter) {
		this.delimter = delimter;
	}

	

}
