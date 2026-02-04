package com.example.voteonline.service;

import com.example.voteonline.dto.AdminResultDto;
import com.example.voteonline.dto.CandidateDto;
import com.example.voteonline.exception.NotFoundException;
import com.example.voteonline.model.Candidate;
import com.example.voteonline.model.Vote;
import com.example.voteonline.repository.CandidateRepository;
import com.example.voteonline.repository.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core voting business logic.
 */
@Service
public class VoteService {

    private final CandidateRepository candidateRepository;
    private final VoteRepository voteRepository;

    public VoteService(CandidateRepository candidateRepository, VoteRepository voteRepository) {
        this.candidateRepository = candidateRepository;
        this.voteRepository = voteRepository;
    }

    /**
     * Returns all candidates to be displayed on the public voting page.
     */
    @Transactional(readOnly = true)
    public List<CandidateDto> listCandidates() {
        return candidateRepository.findAll().stream()
                .map(c -> new CandidateDto(c.getId(), c.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Casts or updates a vote from the given IP address for the given candidate.
     * - Nếu IP chưa vote: tạo mới.
     * - Nếu IP đã vote: cập nhật lại candidate (cho phép chỉnh sửa lựa chọn).
     */
    @Transactional
    public void castVote(Long candidateId, String ipAddress) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new NotFoundException("Candidate not found"));

        Vote vote = voteRepository.findByIpAddress(ipAddress)
                .orElseGet(() -> new Vote(candidate, ipAddress, OffsetDateTime.now()));

        // Nếu vote đã tồn tại thì chỉ cập nhật candidate và thời gian
        vote.setCandidate(candidate);
        vote.setCreatedAt(OffsetDateTime.now());

        voteRepository.save(vote);
    }

    /**
     * Lấy candidateId hiện tại mà IP này đã vote (nếu có).
     */
    @Transactional(readOnly = true)
    public Long getCurrentVoteCandidateId(String ipAddress) {
        return voteRepository.findByIpAddress(ipAddress)
                .map(v -> v.getCandidate().getId())
                .orElse(null);
    }

    /**
     * Returns aggregated results per candidate for admin use only.
     */
    @Transactional(readOnly = true)
    public List<AdminResultDto> getAdminResults() {
        return voteRepository.aggregateResults().stream()
                .map(p -> new AdminResultDto(
                        p.getCandidateId(),
                        p.getCandidateName(),
                        p.getTotalVotes()
                ))
                .collect(Collectors.toList());
    }
}

