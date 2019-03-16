package com.example.com.example;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class AuthClientApplication {

	@Bean
	RestTemplate restTemplate(OAuth2AuthorizedClientService clientService) {
		return new RestTemplateBuilder()
				.interceptors((ClientHttpRequestInterceptor) (httpRequest, bytes, execution) -> {

					OAuth2AuthenticationToken token = OAuth2AuthenticationToken.class
							.cast(SecurityContextHolder.getContext().getAuthentication());

					OAuth2AuthorizedClient client = clientService
							.loadAuthorizedClient(token.getAuthorizedClientRegistrationId(), token.getName());

					httpRequest.getHeaders().add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
					httpRequest.getHeaders().add(HttpHeaders.ACCEPT_CHARSET, StandardCharsets.UTF_8.name());
					httpRequest.getHeaders().add(HttpHeaders.AUTHORIZATION,
							"Bearer " + client.getAccessToken().getTokenValue());
					ClientHttpResponse clientHttpResponse = execution.execute(httpRequest, bytes);
					return clientHttpResponse;
				}).build();
	}

	public static void main(String[] args) {
		SpringApplication.run(AuthClientApplication.class, args);
	}
}

@RestController
@EnableWebMvc
class ProfileRestController {

	// private OAuth2RestTemplate restTemplate;
	private RestTemplate restTemplate;
	private final OAuth2AuthorizedClientService clientService;

	ProfileRestController(RestTemplate restTemplate, OAuth2AuthorizedClientService clientService) {
		this.restTemplate = restTemplate;
		this.clientService = clientService;
	}

	@GetMapping(value = "/")
	PrincipalDetails profile(OAuth2AuthenticationToken token) {

		OAuth2AuthorizedClient client = this.clientService
				.loadAuthorizedClient(token.getAuthorizedClientRegistrationId(), token.getName());
		String uri = client.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri();
		ResponseEntity<PrincipalDetails> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, null,
				PrincipalDetails.class);
		return responseEntity.getBody();

	}
}

class PrincipalDetails {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}

@Configuration
class MiBenefitConfiguration extends WebMvcConfigurationSupport {

	@Bean
	public ObjectMapper getObjectMapper() {
		return new ObjectMapper();
	}

	@Bean
	public MappingJackson2HttpMessageConverter messageConverter() {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setObjectMapper(getObjectMapper());
		return converter;
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(messageConverter());
		addDefaultHttpMessageConverters(converters);
	}
}