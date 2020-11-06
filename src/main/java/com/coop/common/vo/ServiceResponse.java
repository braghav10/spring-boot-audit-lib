package com.coop.common.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import javax.validation.constraints.NotNull;
@Data
public class ServiceResponse<T> {
	
	@JsonInclude(NON_EMPTY)
	private T data;
	
	@NotNull
	private ErrorInfo errorInfo;

}
