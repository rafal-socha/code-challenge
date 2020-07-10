package com.hsbc.digital.codechallenge.api;

import java.util.List;

import javax.validation.Valid;

import com.hsbc.digital.codechallenge.domain.Message;
import com.hsbc.digital.codechallenge.dto.MessageDto;
import com.hsbc.digital.codechallenge.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/users")
class UserController {

	@Autowired
	private UserService service;

	@Operation(summary = "Add post by user. New user will be created if no user exists for given id.",
			requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
					schema = @Schema(example = "{ \"text\": \"string\"}")
			))
	)
	@ResponseStatus(CREATED)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Created"),
			@ApiResponse(responseCode = "400", description = "Bad request. Message can not be longer than 140 characters.", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
	})
	@PostMapping("/{userId}/messages")
	public MessageDto addPost(@PathVariable final Long userId, @RequestBody @Valid final MessageDto messageDto) {
		Message message = service.addPost(userId, new Message(messageDto.text));
		return MessageDto.builder().text(message.getText())
				.createDate(message.getCreateDate())
				.build();
	}

	@Operation(summary = "Get all messages for user.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "404", description = "Not found. User with specified id not found.", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
	})
	@GetMapping("/{userId}/messages")
	public List<MessageDto> getUserPosts(@PathVariable final Long userId) {
		return service.getUserMessages(userId).stream()
				.map(message -> MessageDto.builder().text(message.getText())
						.createDate(message.getCreateDate()).build())
				.collect(toList());
	}

	@Operation(summary = "Follow another user.")
	@ResponseStatus(NO_CONTENT)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "No content"),
			@ApiResponse(responseCode = "404", description = "Not found. Followed or following user not found."),
			@ApiResponse(responseCode = "409", description = "Conflict. User can not follow himself."),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	@PostMapping("/{userId}/follow/{followedUserId}")
	public void followUser(@PathVariable final Long userId, @PathVariable final Long followedUserId) {
		service.followUser(userId, followedUserId);
	}

	@Operation(summary = "Get timeline for user.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "404", description = "Not found. User with specified id not found.", content = @Content()),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content())
	})
	@GetMapping("/{userId}/timeline")
	public List<MessageDto> getTimeline(@PathVariable final Long userId) {
		return service.getTimeline(userId).stream()
				.map(message -> MessageDto.builder().text(message.getText())
						.createDate(message.getCreateDate()).build())
				.collect(toList());
	}
}
