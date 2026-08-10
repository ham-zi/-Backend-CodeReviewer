package com.reviewer.review.model.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reviewer.auth.model.vo.CustomUserDetails;
import com.reviewer.enums.ReviewTypeRole;
import com.reviewer.github.model.dto.GithubCompareResponse;
import com.reviewer.github.model.dto.GithubFileResponse;
import com.reviewer.github.model.service.GithubClient;
import com.reviewer.ollama.client.OllamaClient;
import com.reviewer.project.model.entity.ProjectEntity;
import com.reviewer.project.projectRule.model.repository.ProjectRuleRepository;
import com.reviewer.project.validator.ProjectValidator;
import com.reviewer.review.model.dao.ReviewRepository;
import com.reviewer.review.model.dto.ReviewRequest;
import com.reviewer.review.model.entity.ReviewEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly=true)
public class ReviewService {
	
	private final ProjectValidator projectValidator;
	private final GithubClient githubClient;
	private final OllamaClient ollamaClient;
	private final ReviewAsyncService reviewAsyncService;
	private final ReviewRepository reviewRepository;
	
	@Transactional
	public Long quickReview(CustomUserDetails user, ReviewRequest reviewRequest) {
		ProjectEntity project = projectValidator.existsProject(reviewRequest.projectId());
		projectValidator.checkProjectMember(reviewRequest.projectId(), user.getUserId());
		GithubCompareResponse response = githubClient.compare(project.getGitRepoOwner(), project.getGitRepoName(), reviewRequest.baseBranch(), reviewRequest.headBranch());
		ReviewEntity review = reviewRepository.save(ReviewEntity.of(project,
				                               ReviewTypeRole.BRANCH, 
				                               reviewRequest.baseBranch(),
				                               reviewRequest.headBranch(),
				                               project.getProjectRule()));
		reviewAsyncService.process(review.getReviewId(), response);
		return review.getReviewId();
	}
	
	public String branchReview(ReviewEntity review, GithubCompareResponse res) {
		StringBuilder sb = new StringBuilder();
		sb.append("##리뷰 대상 코드##");
		for (GithubFileResponse file : res.files()) {
		    sb.append("파일: ").append(file.filename()).append("\n");
		    sb.append("변경사항:\n");
		    sb.append(file.patch()).append("\n\n");
		}
		sb.append("##팀 규칙##");
		sb.append(review.getProjectRule().getContent());
	    return ollamaClient.generate(
	        		sb.toString()
	    );
	}
	
	
	public String testOllama(ProjectEntity project, GithubCompareResponse res) {
		StringBuilder sb = new StringBuilder();
		sb.append("##리뷰 대상 코드##");
		sb.append("""
				// UserController.java
					package com.example.user;
					
					import java.util.List;
					
					import org.springframework.web.bind.annotation.GetMapping;
					import org.springframework.web.bind.annotation.PostMapping;
					import org.springframework.web.bind.annotation.RequestBody;
					import org.springframework.web.bind.annotation.RestController;
					
					@RestController
					public class UserController {
					
					    private final UserRepository userRepository;
					    private final UserService userService;
					
					    public UserController(
					            UserRepository userRepository,
					            UserService userService) {
					        this.userRepository = userRepository;
					        this.userService = userService;
					    }
					
					    @GetMapping("/users")
					    public List<User> getUsers() {
					        return userRepository.findAll();
					    }
					
					    @PostMapping("/users")
					    public User createUser(@RequestBody UserCreateRequest request) {
					        return userService.createUser(request);
					    }
					}
								// UserService.java
					package com.example.user;
					
					import java.util.Optional;
					
					import org.springframework.stereotype.Service;
					import org.springframework.transaction.annotation.Transactional;
					
					@Service
					public class UserService {
					
					    private final UserRepository userRepository;
					
					    public UserService(UserRepository userRepository) {
					        this.userRepository = userRepository;
					    }
					
					    @Transactional
					    public User createUser(UserCreateRequest request) {
					
					        User user = new User();
					
					        user.setName(request.name());
					        user.setEmail(request.email());
					
					        return userRepository.save(user);
					    }
					
					    public User getUser(Long userId) {
					
					        Optional<User> user = userRepository.findById(userId);
					
					        return user.get();
					    }
					}	
					// UserRepository.java
					package com.example.user;
					
					import java.util.List;
					
					import org.springframework.data.jpa.repository.JpaRepository;
					
					public interface UserRepository extends JpaRepository<User, Long> {
					
					    List<User> findByName(String name);
					}	
					// User.java
					package com.example.user;
					
					import jakarta.persistence.Entity;
					import jakarta.persistence.GeneratedValue;
					import jakarta.persistence.GenerationType;
					import jakarta.persistence.Id;
					
					@Entity
					public class User {
					
					    @Id
					    @GeneratedValue(strategy = GenerationType.IDENTITY)
					    private Long id;
					
					    private String name;
					
					    private String email;
					
					    protected User() {
					    }
					
					    public void setName(String name) {
					        this.name = name;
					    }
					
					    public void setEmail(String email) {
					        this.email = email;
					    }
					
					    public Long getId() {
					        return id;
					    }
					
					    public String getName() {
					        return name;
					    }
					
					    public String getEmail() {
					        return email;
					    }
					}
					// UserCreateRequest.java
					package com.example.user;
					
					public record UserCreateRequest(
					        String name,
					        String email
					) {
					}			
				  """);
		sb.append("##팀 규칙##");
		sb.append(project.getProjectRule().getContent());
	    return ollamaClient.generate(
	        		sb.toString()
	        		);

	}
	
}
