package com.coop;

import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import com.google.gson.Gson;

import lombok.Getter;
import lombok.Setter;

@Aspect
@Order(value = 0)
@Component
@ConfigurationProperties(prefix = "endpoint")
@Setter
@Getter
public class RestAspect {

	@Autowired
	private Environment env;

	private Map<String, String> identifiers;

	private static final Logger logger = LoggerFactory.getLogger(RestAspect.class);

	@Pointcut("within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.stereotype.Controller *)")
	public void ControllerClass() {
	}

	@Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping) || @annotation(org.springframework.web.bind.annotation.GetMapping) || @annotation(org.springframework.web.bind.annotation.PostMapping) || @annotation(org.springframework.web.bind.annotation.DeleteMapping) || @annotation(org.springframework.web.bind.annotation.PutMapping)")
	public void ControllerMethod() {
	}

	@Around("ControllerClass() && ControllerMethod()")
	public Object controllerAuditing(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

		long start = System.currentTimeMillis();
		Object value = null;
		Object[] obj = proceedingJoinPoint.getArgs();
		Object requestOb = null;
		if (obj.length >= 2) {
			requestOb = obj[1];
		}
		String apiRequest = null;
		Gson gson = new Gson();
		if (requestOb != null) {
			apiRequest = gson.toJson(requestOb);
		}
		
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		MDC.put("request_path", path);

		if (StringUtils.isNotBlank(path)) {
			String result = identifiers.keySet().stream().filter(x -> path.contains(x))
					.map(x -> x).collect(Collectors.joining());

			MDC.put("endpoint_identifier", identifiers.get(result));
		}

		value = proceedingJoinPoint.proceed();
		long executionTime = System.currentTimeMillis() - start;
		//ResponseEntity<ServiceResponse<Object>> finlaResponse = buildSuccessResponseEntity(request, value);

		MDC.put("apiRequest", apiRequest);
		MDC.put("log_type", "A");
		String apiResponse = gson.toJson(value);
		MDC.put("apiResponse", apiResponse);
		MDC.put("total_time", executionTime);
		logger.error("Response from controller");
		MDC.clear();
		return value;

	}

	

	@Around("@annotation(com.coop.Loggable)")
	public Object externalAudit(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

		long start = System.currentTimeMillis();
		Object value = null;
		Object[] obj = proceedingJoinPoint.getArgs();
		Class<? extends Object> className = proceedingJoinPoint.getTarget().getClass();
		Object requestOb = null;
		String extSystem = null;
		if (obj.length == 1) {
			requestOb = obj[0];
		}
		Gson gson = new Gson();
		if (requestOb != null) {
			String apiRequest = gson.toJson(requestOb);
			MDC.put("ext_apiRequest", apiRequest);
			if (className.getCanonicalName().contains("Drools")) {
				extSystem = "Drools";
				MDC.put("ext_system", extSystem);
			} else if (className.getCanonicalName().contains("Mysql")) {
				extSystem = "Mysql";
				MDC.put("ext_system", extSystem);
			}
		}
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

		value = proceedingJoinPoint.proceed();
		long executionTime = System.currentTimeMillis() - start;
		MDC.put("log_type", "A");
		String apiResponse = gson.toJson(value);
		MDC.put("ext_apiResponse", apiResponse);
		MDC.put("ext_total_time", executionTime);
		logger.error("Response from {} ExtSystem", extSystem);
		MDC.put("log_type", "L");
		MDC.remove("ext_apiResponse");
		MDC.remove("ext_total_time");
		MDC.remove("ext_system");
		MDC.remove("ext_apiRequest");
		return value;

	}
}
