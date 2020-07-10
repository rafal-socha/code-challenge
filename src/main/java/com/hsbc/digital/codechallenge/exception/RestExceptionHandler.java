package com.hsbc.digital.codechallenge.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(value = NotFoundException.class)
	protected ResponseEntity handleNotFound(RuntimeException exception, WebRequest request) {
		return new ResponseEntity(exception.getMessage(), NOT_FOUND);
	}

	@ExceptionHandler(value = ConflictException.class)
	protected ResponseEntity handleConflict(RuntimeException exception, WebRequest request) {
		return new ResponseEntity(exception.getMessage(), CONFLICT);
	}
}
