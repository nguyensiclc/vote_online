package com.example.voteonline.dto;

/**
 * DTO exposed to the public API representing a candidate.
 */
public class CandidateDto {

    private Long id;
    private String name;

    public CandidateDto() {
    }

    public CandidateDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

