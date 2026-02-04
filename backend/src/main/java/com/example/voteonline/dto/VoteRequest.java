package com.example.voteonline.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for casting a vote.
 */
public class VoteRequest {

    @NotNull
    private Long candidateId;

    public VoteRequest() {
    }

    public VoteRequest(Long candidateId) {
        this.candidateId = candidateId;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }
}

