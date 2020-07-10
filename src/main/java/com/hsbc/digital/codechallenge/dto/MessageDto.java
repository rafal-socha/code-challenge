package com.hsbc.digital.codechallenge.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.Size;

import lombok.Builder;

@Builder
public class MessageDto implements Serializable {
	@Size(max = 140)
	public String text;

	public LocalDateTime createDate;
}
