package com.shepherdmoney.interviewproject.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.springframework.http.HttpStatus;
@Data
@AllArgsConstructor
public class InValidRequestException extends RuntimeException{

    private final String errorCode;
    private final String message;
    private final HttpStatus httpStatus;


}
