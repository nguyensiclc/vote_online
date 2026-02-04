package com.example.voteonline.dto;

/**
 * DTO returned by the admin API with aggregated votes per candidate.
 */
public class AdminResultDto {

    private Long candidateId;
    private String candidateName;
    private Long totalVotes;

    public AdminResultDto() {
    }

    public AdminResultDto(Long candidateId, String candidateName, Long totalVotes) {
        this.candidateId = candidateId;
        this.candidateName = candidateName;
        this.totalVotes = totalVotes;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(Long candidateId) {
        this.candidateId = candidateId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public Long getTotalVotes() {
        return totalVotes;
    }

    public void setTotalVotes(Long totalVotes) {
        this.totalVotes = totalVotes;
    }
}

