package com.hsbc.digital.codechallenge.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.hsbc.digital.codechallenge.domain.Message;
import com.hsbc.digital.codechallenge.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;

class UserServiceTest {

	private static final String TEXT_1 = "text 1";

	private static final String TEXT_2 = "text 2";

	private static final String TEXT_3 = "text 3";

	private static final String TEXT_4 = "text 4";

	private final Map<Long, User> users = new HashMap<>();

	private final UserService tested = new UserService(users);

	@AfterEach
	void afterEach() {
		users.clear();
	}

	@Test
	void shouldCreateUserAndAddPost() {
		//when
		Message response = tested.addPost(1L, new Message(TEXT_1));

		//then
		assertThat(response.getText()).isEqualTo(TEXT_1);
		assertThat(users).hasSize(1);
		assertThat(users.keySet()).containsOnly(1L);
	}

	@Test
	void shouldAddMessageForExistingUser() {
		//given
		addUser(1L, TEXT_1, now());

		//when
		Message response = tested.addPost(1L, new Message(TEXT_2, now().plusMinutes(1)));

		//then
		assertThat(response.getText()).isEqualTo(TEXT_2);
		assertThat(users).hasSize(1);
		assertThat(users.keySet()).containsOnly(1L);
		assertThat(users.get(1L).getMessages()).extracting(Message::getText)
				.containsExactly(TEXT_2, TEXT_1);
	}

	private void addUser(final Long userId, final String text, final LocalDateTime createdDate) {
		User user = ofNullable(users.get(userId)).orElse(new User());
		user.addMessage(new Message(text, createdDate));
		users.put(userId, user);
	}

	@Test
	void shouldGetUserMessages() {
		addUser(1L, TEXT_1, now());
		addUser(1L, TEXT_2, now().plusMinutes(1));

		//when
		List<Message> response = tested.getUserMessages(1L);

		//then
		assertThat(response).hasSize(2);
		assertThat(response).extracting(Message::getText)
				.containsExactly(TEXT_2, TEXT_1);
	}

	@Test
	void shouldFollowUser() {
		//given
		addUser(1L, TEXT_1, now());
		addUser(2L, TEXT_1, now());
		addUser(3L, TEXT_1, now());

		//when
		tested.followUser(3L, 1L);
		tested.followUser(3L, 2L);

		//then
		assertThat(users.get(3L)).extracting(User::getFollowedUsers)
				.isEqualTo(new HashSet<>(asList(1L, 2L)));
	}

	@Test
	void shouldGetTimeline() {
		//given
		addUser(1L, TEXT_1, now());
		addUser(2L, TEXT_2, now().plusMinutes(1));
		addUser(1L, TEXT_3, now().plusMinutes(2));
		addUser(3L, TEXT_4, now().plusMinutes(3));
		users.get(3L).addFollowedUser(1L);
		users.get(3L).addFollowedUser(2L);

		//when
		List<Message> response = tested.getTimeline(3L);

		//then
		assertThat(response).extracting(Message::getText)
				.containsExactly(TEXT_3, TEXT_2, TEXT_1);
	}
}
