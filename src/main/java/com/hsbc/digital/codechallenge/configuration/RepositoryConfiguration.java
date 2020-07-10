package com.hsbc.digital.codechallenge.configuration;

import java.util.HashMap;
import java.util.Map;

import com.hsbc.digital.codechallenge.domain.User;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfiguration {
	@Bean
	public Map<Long, User> getUsers() {
		return new HashMap<>();
	}
}
