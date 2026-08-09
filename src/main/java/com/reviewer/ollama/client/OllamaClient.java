package com.reviewer.ollama.client;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.reviewer.ollama.model.dto.OllamaRequest;
import com.reviewer.ollama.model.dto.OllamaResponse;

import reactor.netty.http.client.HttpClient;

@Component
public class OllamaClient {

	private final RestClient restClient;
	private final String model;
	
	public OllamaClient(
		@Value("${app.ollama.base-url}") String baseUrl,
		@Value("${app.ollama.model}") String model) {
		
		HttpClient httpClient = HttpClient.create()
				.responseTimeout(Duration.ofMinutes(5));
		
		ReactorClientHttpRequestFactory requestFactory =
				new ReactorClientHttpRequestFactory(httpClient);
		this.restClient = RestClient.builder()
				                    .baseUrl(baseUrl)
				                    .requestFactory(requestFactory)
				                    .build();
		this.model = model;
		
	}
	
	public String generate(String prompt) {
		OllamaRequest request = new OllamaRequest(model,"너는 Java/Spring 전문 코드 리뷰어다. 반드시 한국어로 답변한다.", prompt, false);
		
		OllamaResponse response = restClient.post().uri("/api/generate")
												   .body(request)
												   .retrieve()
												   .body(OllamaResponse.class);
		
		if(response == null) {
			throw new IllegalStateException("Ollama 응답이 존재하지 않습니다.");
		}
		
		return response.response();
	}
}
