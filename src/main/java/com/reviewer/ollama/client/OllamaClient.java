package com.reviewer.ollama.client;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.reviewer.enums.ReviewTypeRole;
import com.reviewer.exception.common.NotFoundException;
import com.reviewer.ollama.model.dto.OllamaOptions;
import com.reviewer.ollama.model.dto.OllamaRequest;
import com.reviewer.ollama.model.dto.OllamaResponse;
import com.reviewer.system.model.Entity.SystemSettingEntity;
import com.reviewer.system.model.dao.SystemSettingRepository;
import com.reviewer.system.model.service.SystemPromptService;

import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.HttpClient;

@Component
@Slf4j
public class OllamaClient {

	private final RestClient restClient;
	private final String baseUrl;
	private final String model;
	private final boolean stream;
	private final String format;
	private final Integer numCtx;
	private final Double temperature;
	private final SystemPromptService systemPromptService;
	
	public OllamaClient(
		@Value("${app.ollama.base-url}") 
		String baseUrl,
		@Value("${app.ollama.model}") 
		String model,
		@Value("${app.ollama.stream}")
		boolean stream,
		@Value("${app.ollama.format}")
		String format,
		@Value("${app.ollama.num-ctx}")
		Integer numCtx,
		@Value("${app.ollama.temperature}")
		Double temperature,
		SystemPromptService systemPromptService) {
		
		HttpClient httpClient = HttpClient.create()
				.responseTimeout(Duration.ofMinutes(5));
		
		ReactorClientHttpRequestFactory requestFactory =
				new ReactorClientHttpRequestFactory(httpClient);
		this.restClient = RestClient.builder()
				                    .baseUrl(baseUrl)
				                    .requestFactory(requestFactory)
				                    .build();
		this.model = model;
		this.baseUrl = baseUrl;
		this.stream = stream;
		this.format = format;
		this.numCtx = numCtx;
		this.temperature = temperature;
		this.systemPromptService = systemPromptService;
	}
	
	public OllamaResponse quickReview(String prompt) {
		String systemPrompt = systemPromptService.findCurrentPrompt(ReviewTypeRole.QUICK);
		OllamaRequest request = new OllamaRequest(model, systemPrompt ,prompt, stream, format, new OllamaOptions(numCtx, temperature));
		
		OllamaResponse response = restClient.post().uri("/api/generate")
												   .body(request)
												   .retrieve()
												   .body(OllamaResponse.class);
		
		if(response == null) {
			throw new IllegalStateException("Ollama 응답이 존재하지 않습니다.");
		}
		
		return response;
	}
	public OllamaResponse branchReview(String prompt) {
		String systemPrompt = systemPromptService.findCurrentPrompt(ReviewTypeRole.BRANCH);
		OllamaRequest request = new OllamaRequest(model, systemPrompt ,prompt, stream, format, new OllamaOptions(numCtx, temperature));
		
		OllamaResponse response = restClient.post().uri("/api/generate")
				.body(request)
				.retrieve()
				.body(OllamaResponse.class);
		
		if(response == null) {
			throw new IllegalStateException("Ollama 응답이 존재하지 않습니다.");
		}
		
		return response;
	}
}
