package com.coop.common.vo;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@Builder
public class ErrorInfo {

	private String timeStamp;
	private String correlationId;
	private String errorCode;
	private String errorMessage;
	private String jn;

}
