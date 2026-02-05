package com.example.voteonline.repository;

import com.example.voteonline.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for Candidate entity.
 */
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    java.util.List<Candidate> findByPollMeta_Id(Long pollId);
    void deleteByPollMeta_Id(Long pollId);
}

