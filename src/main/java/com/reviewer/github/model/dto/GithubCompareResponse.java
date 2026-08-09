package com.reviewer.github.model.dto;

import java.util.List;

public record GithubCompareResponse(
		String status,
		Integer ahead_by,
		Integer behind_by,
		List<GithubFileResponse> files
		) {

}
