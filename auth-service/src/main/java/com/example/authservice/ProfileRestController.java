package com.example.authservice;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileRestController {

	@GetMapping("/resources/userinfo")
	Map<String, String> profile(Principal principal) {
		return Collections.singletonMap("name", principal.getName());
	}
}
