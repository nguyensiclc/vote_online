package com.example.voteonline.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

/**
 * Vote entity representing a single vote from an IP address for a candidate.
 */
@Entity
@Table(
        name = "votes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_votes_poll_ip", columnNames = {"poll_meta_id", "ip_address"})
        }
)
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poll_meta_id", nullable = false)
    private PollMeta pollMeta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Column(name = "ip_address", nullable = false, length = 64)
    private String ipAddress;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public Vote() {
    }

    public Vote(PollMeta pollMeta, Candidate candidate, String ipAddress, OffsetDateTime createdAt) {
        this.pollMeta = pollMeta;
        this.candidate = candidate;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
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

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

