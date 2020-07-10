package com.hsbc.digital.codechallenge.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.hsbc.digital.codechallenge.domain.Message;
import com.hsbc.digital.codechallenge.domain.User;
import com.hsbc.digital.codechallenge.exception.ConflictException;
import com.hsbc.digital.codechallenge.exception.NotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException.Conflict;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@Service
public class UserService {

	private final Map<Long, User> users;

	public UserService(@Autowired final Map<Long, User> users) {
		this.users = users;
	}

	public Message addPost(final Long userId, final Message message) {
		User user = Optional.ofNullable(users.get(userId)).orElse(new User());
		user.addMessage(message);
		users.put(userId, user);
		return message;
	}

	public List<Message> getUserMessages(final Long userId) {
		return Optional.ofNullable(users.get(userId))
				.orElseThrow(() -> notFoundException(userId))
				.getMessages();
	}

	private NotFoundException notFoundException(final Long userId) {
		return new NotFoundException(format("User with id: %d not found.", userId));
	}

	public void followUser(final Long userId, final Long followedUserId) {
		if(userId.equals(followedUserId)) {
			throw new ConflictException("User can not follow himself.");
		}

		Optional.ofNullable(users.get(followedUserId))
				.orElseThrow(() -> notFoundException(followedUserId));
		Optional.ofNullable(users.get(userId))
				.orElseThrow(() -> notFoundException(userId))
				.addFollowedUser(followedUserId);
	}

	public List<Message> getTimeline(final Long userId) {
		return Optional.ofNullable(users.get(userId)).orElseThrow(() -> notFoundException(userId))
				.getFollowedUsers().stream()
				.flatMap(followedUserId -> users.get(followedUserId).getMessages().stream())
				.sorted(this::compareMessages)
				.collect(toList());
	}

	private int compareMessages(final Message o1, final Message o2) {
		if (o1.getCreateDate().isBefore(o2.getCreateDate())) {
			return 1;
		}
		if (o1.getCreateDate().equals(o2.getCreateDate())) {
			return 0;
		}
		return -1;
	}
}
