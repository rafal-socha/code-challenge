package com.hsbc.digital.codechallenge.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.SerializationUtils;

@EqualsAndHashCode
public final class User {

	private final LinkedList<Message> messages = new LinkedList<>();

	private final Set<Long> followedUsers = new HashSet<>();

	public List<Message> getMessages() {
		return SerializationUtils.clone(messages);
	}

	public Collection<Long> getFollowedUsers() {
		return SerializationUtils.clone((HashSet) followedUsers);
	}

	public void addMessage(final Message message) {
		messages.addFirst(message);
	}

	public void addFollowedUser(final Long followedUserId) {
		followedUsers.add(followedUserId);
	}

}
