package com.reviewer.review.model.dto;

public record ReviewRequest(String baseBranch, String headBranch, Long projectId) {

}
