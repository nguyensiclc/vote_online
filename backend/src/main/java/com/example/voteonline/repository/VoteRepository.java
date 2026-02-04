package com.example.voteonline.repository;

import com.example.voteonline.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Vote entity.
 */
public interface VoteRepository extends JpaRepository<Vote, Long> {

    boolean existsByIpAddress(String ipAddress);

    Optional<Vote> findByIpAddress(String ipAddress);

    @Query("""
            select v.candidate.id as candidateId, v.candidate.name as candidateName, count(v) as totalVotes
            from Vote v
            group by v.candidate.id, v.candidate.name
            order by v.candidate.id
            """)
    List<ResultProjection> aggregateResults();

    interface ResultProjection {
        Long getCandidateId();

        String getCandidateName();

        Long getTotalVotes();
    }
}

