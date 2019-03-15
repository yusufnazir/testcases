package com.example.authservice;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SimpleUserDetailsService implements UserDetailsService {

	private final Map<String, UserDetails> users = new ConcurrentHashMap<>();

	SimpleUserDetailsService() {
		Arrays.asList("josh", "rob", "joe")
				.forEach(username -> this.users.putIfAbsent(username,
						new CustomUser(username, "$2a$10$WFqkIMKDW9u0bPysCvrwjurXDVeIESGMrRh/JifPq7XDQ/oUBmyDi", true,
								true, true, true, AuthorityUtils.createAuthorityList("USER"))));
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return this.users.get(username);
	}
}
