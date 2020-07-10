package com.hsbc.digital.codechallenge.dto;

import java.util.List;
import java.util.Set;

import lombok.Builder;

@Builder
public class UserDto {
	public List<MessageDto> messages;

	public Set<Long> followedUsers;
}
