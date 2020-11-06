package com.coop.common.config;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang3.StringUtils;

public class CustomHttpServletRequest extends HttpServletRequestWrapper{

	private Map<String,String> headerMap = null;
	public CustomHttpServletRequest(HttpServletRequest request) {
		super(request);
		headerMap = new HashMap<>();
	}
	public void addheader(String name, String value){
		headerMap.put(name, value);
	}
	
	public String getheader(String name){
		String superHeader = super.getHeader(name);
		if(StringUtils.isBlank(superHeader)){
			superHeader = headerMap.get(name);
		}
		return superHeader;
	}

}
