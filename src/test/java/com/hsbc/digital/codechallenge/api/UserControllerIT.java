package com.hsbc.digital.codechallenge.api;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;

import com.hsbc.digital.codechallenge.CodeChallengeApplication;
import com.hsbc.digital.codechallenge.domain.Message;
import com.hsbc.digital.codechallenge.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
		webEnvironment = MOCK,
		classes = CodeChallengeApplication.class
)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
class UserControllerIT {

	private final static String TEXT_140_CHARACTERS = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim";

	private final static String TEXT_2 = "Text 2";

	private final static String TEXT_3 = "Text 3";

	private final static String TEXT_141_CHARACTERS = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim.";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private Map<Long, User> users;

	@AfterEach
	void afterEach() {
		users.clear();
	}

	private String messageJson(final String content) {
		return "{\"text\": \"" + content + "\"}";
	}

	private void addMessage(final Long userId, final String text, final LocalDateTime createDate) {
		User user = ofNullable(users.get(userId)).orElse(new User());
		user.addMessage(new Message(text, createDate));
		users.put(userId, user);
	}

	@Nested
	class PostMessage {
		@Test
		void shouldAddNewUserAndPostMessage() throws Exception {
			mockMvc.perform(post("/users/1/messages")
					.contentType(APPLICATION_JSON)
					.content(messageJson(TEXT_140_CHARACTERS)))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("text").value(TEXT_140_CHARACTERS));
			assertThat(users).hasSize(1);
			assertThat(users.keySet()).containsOnly(1L);
			assertThat(users.get(1L).getMessages()).extracting(Message::getText)
					.containsExactly(TEXT_140_CHARACTERS);
		}

		@Test
		void shouldPostMessageByExistingUser() throws Exception {
			//given
			users.put(1L, new User());

			//when then
			mockMvc.perform(post("/users/1/messages")
					.contentType(APPLICATION_JSON)
					.content(messageJson(TEXT_140_CHARACTERS)))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("text").value(TEXT_140_CHARACTERS));
			assertThat(users).hasSize(1);
			assertThat(users.keySet()).containsOnly(1L);
			assertThat(users.get(1L).getMessages()).extracting(Message::getText)
					.containsExactly(TEXT_140_CHARACTERS);
		}

		@Test
		void shouldReturnBadRequestWhenMessageLongerThan140Characters() throws Exception {
			mockMvc.perform(post("/users/1/messages")
					.contentType(APPLICATION_JSON)
					.content(messageJson(TEXT_141_CHARACTERS)))
					.andExpect(status().isBadRequest());
			assertThat(users).hasSize(0);
		}
	}

	@Nested
	class GetAllMessagesForUser {
		@Test
		void shouldGetAllMessagesForUser() throws Exception {
			//given
			addMessage(1L, TEXT_140_CHARACTERS, now());
			addMessage(1L, TEXT_2, now());

			//when then
			mockMvc.perform(get("/users/1/messages"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$[0].text").value(TEXT_2))
					.andExpect(jsonPath("$[1].text").value(TEXT_140_CHARACTERS));
		}

		@Test
		void shouldReturnNotFoundWhenRequestingMessagesForNotExistingUser() throws Exception {
			//when then
			mockMvc.perform(get("/users/1/messages"))
					.andExpect(status().isNotFound());
		}
	}

	@Nested
	class FollowUser {
		@Test
		void shouldFollowUser() throws Exception {
			//given
			postMessage(1L, messageJson(TEXT_140_CHARACTERS));
			postMessage(2L, messageJson(TEXT_140_CHARACTERS));
			postMessage(3L, messageJson(TEXT_2));

			//when then
			mockMvc.perform(post("/users/3/follow/1"))
					.andExpect(status().isNoContent());
			mockMvc.perform(post("/users/3/follow/2"))
					.andExpect(status().isNoContent());
			assertEquals(new HashSet<>(asList(1L, 2L)), users.get(3L).getFollowedUsers());
		}

		private void postMessage(final Long userId, final String payload) throws Exception {
			mockMvc.perform(post("/users/" + userId + "/messages")
					.contentType(APPLICATION_JSON)
					.content(payload));
		}

		@Test
		void shouldReturnConflictWhenUserTryingToFollowHimself() throws Exception {
			//when then
			mockMvc.perform(post("/users/1/follow/1"))
					.andExpect(status().isConflict());
		}

		@Test
		void shouldReturnNotFoundWhenTryingToFollowNotExistingUser() throws Exception {
			//when then
			mockMvc.perform(post("/users/1/follow/2"))
					.andExpect(status().isNotFound())
					.andExpect(content().string("User with id: 2 not found."));
		}

		@Test
		void shouldReturnNotFoundWhenFollowerNotExists() throws Exception {
			//given
			users.put(2L, new User());

			//when then
			mockMvc.perform(post("/users/1/follow/2"))
					.andExpect(status().isNotFound())
					.andExpect(content().string("User with id: 1 not found."));
		}
	}

	@Nested
	class GetTimeline {
		@Test
		void shouldGetTimeline() throws Exception {
			//given
			addMessage(1L, TEXT_140_CHARACTERS, now());
			addMessage(2L, TEXT_2, now().plusMinutes(1));
			addMessage(1L, TEXT_3, now().plusMinutes(2));
			addMessage(3L, TEXT_140_CHARACTERS, now().plusMinutes(3));
			users.get(3L).addFollowedUser(1L);
			users.get(3L).addFollowedUser(2L);

			//when then
			mockMvc.perform(get("/users/3/timeline"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$[0].text").value(TEXT_3))
					.andExpect(jsonPath("$[1].text").value(TEXT_2))
					.andExpect(jsonPath("$[2].text").value(TEXT_140_CHARACTERS));
		}

		@Test
		void shouldReturnNotFoundWhenRequestinTimelineOfNotExistingUser() throws Exception {
			//when then
			mockMvc.perform(get("/users/1/timeline"))
					.andExpect(status().isNotFound())
					.andExpect(content().string("User with id: 1 not found."));
		}
	}
}
