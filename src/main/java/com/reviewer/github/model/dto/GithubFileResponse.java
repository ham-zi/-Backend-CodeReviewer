package com.reviewer.github.model.dto;

public record GithubFileResponse(
		String filename,
		String status,
		Integer additions,
		Integer deletions,
		String patch
		) {

}
