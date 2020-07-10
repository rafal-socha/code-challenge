package com.hsbc.digital.codechallenge.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.SerializationUtils;

@EqualsAndHashCode
public final class Message implements Serializable {

	private final String text;

	private final LocalDateTime createDate;

	public Message(final String text) {
		this.text = text;
		this.createDate = LocalDateTime.now();
	}

	public Message(final String text, final LocalDateTime createDate) {
		this.text = text;
		this.createDate = createDate;
	}

	public String getText() {
		return text;
	}

	public LocalDateTime getCreateDate() {
		return SerializationUtils.clone(createDate);
	}

}
