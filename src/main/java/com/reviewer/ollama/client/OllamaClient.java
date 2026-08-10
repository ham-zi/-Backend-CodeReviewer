package com.reviewer.ollama.client;

import java.time.Duration;
import java.util.Map;

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
		String systemPrompt = """
								##역할 지정##
								너는 Java/Spring 프로젝트의 코드 리뷰어다.

								너의 역할은 제공된 코드와 팀 규칙을 비교하여, 직접 확인 가능한 근거를 기반으로 규칙 위반 여부를 판정하는 것이다.
								
								설명 문장은 한국어로 작성한다.
								인사말, 안내문, 서론, 결론, 불필요한 일반론은 작성하지 않는다.
								JSON 앞뒤에 어떠한 문장도 작성하지 않는다.
								
								##시스템 규칙##
								1. 제공된 코드와 팀 규칙만을 근거로 판정한다.
								2. 코드에 없는 클래스, 메서드, 필드, setter, annotation, 로직을 추측하지 않는다.
								3. 각 규칙은 VIOLATION, PASS, NOT_APPLICABLE, INSUFFICIENT_CONTEXT 중 하나로 판정한다.
								4. VIOLATION은 코드에서 위반 사실을 직접 확인할 수 있을 때만 사용한다.
								5. PASS는 해당 규칙의 대상이 존재하며 준수 사실을 직접 확인할 수 있을 때만 사용한다.
								6. 규칙을 적용할 대상 자체가 없으면 NOT_APPLICABLE로 판정한다.
								7. 관련 코드는 존재하지만 정보가 부족하면 INSUFFICIENT_CONTEXT로 판정한다.
								8. VIOLATION 판정에는 반드시 실제 코드 근거와 위치를 제시한다.
								9. 확신이 없다면 위반을 추측하지 말고 INSUFFICIENT_CONTEXT로 판정한다.
						
								정확한 리뷰가 많은 리뷰보다 중요하다.
								존재하지 않는 문제를 만들어내지 않는다.
								
								##응답 형식##
								이 형식으로 코드리뷰 개수만큼 객체형태로 만들어서 응답해라.
								{
								  "reviews": [
								    {
								      "title": "Controller에서 Repository를 직접 호출하고 있습니다.",
								      "status": "VIOLATION",
								      "location": "UserController#getUsers",
								      "evidence": "return userRepository.findAll();",
								      "description": "UserController의 getUsers()가 UserRepository를 직접 호출하고 있습니다. 현재 팀 규칙에서는 Controller가 HTTP 요청과 응답 처리에 집중하고 실제 처리는 Service 계층에 위임하도록 요구하고 있습니다.",
								      "suggestion": "UserService에 조회 기능을 이동하고 Controller에서는 Service만 호출하도록 변경하세요."
								    }
								  ],
								
								  "passed": [
								    "Repository는 데이터 접근 책임만 가진다.",
								    "쓰기 작업에는 @Transactional을 적용한다.",
								    "DTO와 Entity의 역할을 분리한다."
								  ],
								
								  "notApplicable": [
								    "Long 객체의 값 비교에는 == 대신 equals()를 사용한다.",
								    "연관관계는 기본적으로 LAZY 로딩을 사용한다."
								  ],
								
								  "insufficientContext": [
								    {
								      "rule": "중복되는 검증 로직은 Validator로 분리한다.",
								      "reason": "현재 제공된 코드만으로 다른 검증 로직과의 중복 여부를 확인할 수 없습니다."
								    }
								  ]
								}
							  """;
		OllamaRequest request = new OllamaRequest(model, systemPrompt ,prompt, false, "json", Map.of("temperature", 0.0));
		
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
