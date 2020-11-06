package com.coop.common.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.coop.common.vo.ClientHeaders;
import com.coop.common.vo.ErrorInfo;
import com.coop.common.vo.ServiceResponse;

@ControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    	return returnType.getContainingClass().isAnnotationPresent(RestController.class);	
    	 
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        return buildSuccessResponseEntity(request, body);
    }
    
    private <T> ServiceResponse<T> buildSuccessResponseEntity(ServerHttpRequest request,
			Object body) {
    	
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyy hh:mm:ss");
	    HttpServletRequest httpServletRequest = ((ServletServerHttpRequest) request).getServletRequest();
		CustomHttpServletRequest customHttpsevReq = new CustomHttpServletRequest((HttpServletRequest) httpServletRequest);
		ClientHeaders clientHeaders = (ClientHeaders) customHttpsevReq.getAttribute("ClientHeaders");
		String correlationId = "";
		if(clientHeaders!=null) {
			correlationId =clientHeaders.getCorrelationId();
		}
		ErrorInfo errorInfo = ErrorInfo.builder().timeStamp(LocalDateTime.now().format(formatter))
				.correlationId(correlationId).errorCode("00").errorMessage("Sucsess")
				.jn(System.getProperty("server.id")).build();
		ServiceResponse serviceResponse = new ServiceResponse<>();
		serviceResponse.setData(body);
		serviceResponse.setErrorInfo(errorInfo);
		return serviceResponse;

	}
}