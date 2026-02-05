package com.example.voteonline.model;

import jakarta.persistence.*;

/**
 * Candidate entity representing an art performance that can receive votes.
 */
@Entity
@Table(name = "candidates")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poll_meta_id", nullable = false)
    private PollMeta pollMeta;

    @Column(nullable = false)
    private String name;

    public Candidate() {
    }

    public Candidate(PollMeta pollMeta, String name) {
        this.pollMeta = pollMeta;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PollMeta getPollMeta() {
        return pollMeta;
    }

    public void setPollMeta(PollMeta pollMeta) {
        this.pollMeta = pollMeta;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

