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

    boolean existsByIpAddressAndPollMeta_Id(String ipAddress, Long pollId);

    Optional<Vote> findByIpAddressAndPollMeta_Id(String ipAddress, Long pollId);

    @Query("""
            select v.candidate.id as candidateId, v.candidate.name as candidateName, count(v) as totalVotes
            from Vote v
            where v.pollMeta.id = :pollId
            group by v.candidate.id, v.candidate.name
            order by v.candidate.id
            """)
    List<ResultProjection> aggregateResultsByPoll(Long pollId);

    interface ResultProjection {
        Long getCandidateId();

        String getCandidateName();

        Long getTotalVotes();
    }

    void deleteByCandidate_Id(Long candidateId);

    void deleteByCandidate_PollMeta_Id(Long pollId);

    void deleteByPollMeta_Id(Long pollId);
}
