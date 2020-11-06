package com.coop.common.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import com.coop.common.vo.ClientHeaders;
import com.google.gson.Gson;

@Component
public class AuthFilter extends GenericFilterBean {

	@Autowired
	private Environment env;

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		CustomHttpServletRequest customHttpsevReq = new CustomHttpServletRequest((HttpServletRequest) req);
		MDC.put("service", env.getProperty("spring.application.name"));
		MDC.put("log_type", "L");
		MDC.put("service_identifier", env.getProperty("service.identifier"));
		String clientHeadersjson = customHttpsevReq.getHeader("clientHeaders");
		if (StringUtils.isNoneBlank(clientHeadersjson)) {
			ClientHeaders ch = new Gson().fromJson(clientHeadersjson, ClientHeaders.class);
			MDC.put("correlationId", ch.getCorrelationId());
			customHttpsevReq.setAttribute("ClientHeaders", ch);
		}
		chain.doFilter(customHttpsevReq, res);
	}

}
