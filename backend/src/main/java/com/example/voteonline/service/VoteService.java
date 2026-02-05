package com.example.voteonline.service;

import com.example.voteonline.dto.AdminResultDto;
import com.example.voteonline.dto.CandidateDto;
import com.example.voteonline.exception.NotFoundException;
import com.example.voteonline.model.Candidate;
import com.example.voteonline.model.Vote;
import com.example.voteonline.model.PollMeta;
import com.example.voteonline.repository.PollMetaRepository;
import com.example.voteonline.repository.CandidateRepository;
import com.example.voteonline.repository.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core voting business logic.
 */
@Service
public class VoteService {

    private final CandidateRepository candidateRepository;
    private final VoteRepository voteRepository;
    private final PollMetaRepository pollMetaRepository;
    private static final int COOLDOWN_SECONDS = 10;

    public VoteService(CandidateRepository candidateRepository, VoteRepository voteRepository,
            PollMetaRepository pollMetaRepository) {
        this.candidateRepository = candidateRepository;
        this.voteRepository = voteRepository;
        this.pollMetaRepository = pollMetaRepository;
    }

    /**
     * Returns all candidates to be displayed on the public voting page.
     */
    @Transactional(readOnly = true)
    public List<CandidateDto> listCandidates() {
        Long pollId = getCurrentPollId();
        return candidateRepository.findByPollMeta_Id(pollId).stream()
                .map(c -> new CandidateDto(c.getId(), c.getName()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CandidateDto> listCandidatesByPollId(Long pollId) {
        return candidateRepository.findByPollMeta_Id(pollId).stream()
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
        Long pollId = getCurrentPollId();
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new NotFoundException("Candidate not found"));

        OffsetDateTime now = OffsetDateTime.now();
        Vote existing = voteRepository.findByIpAddressAndPollMeta_Id(ipAddress, pollId).orElse(null);
        if (existing != null) {
            long since = Duration.between(existing.getCreatedAt(), now).getSeconds();
            if (since < COOLDOWN_SECONDS) {
                throw new IllegalStateException("Cooldown not passed");
            }
        }

        PollMeta poll = pollMetaRepository.findById(pollId).orElseThrow();
        Vote vote = existing != null ? existing : new Vote(poll, candidate, ipAddress, now);

        // Nếu vote đã tồn tại thì chỉ cập nhật candidate và thời gian
        vote.setCandidate(candidate);
        vote.setCreatedAt(now);

        voteRepository.save(vote);
    }

    /**
     * Lấy candidateId hiện tại mà IP này đã vote (nếu có).
     */
    @Transactional(readOnly = true)
    public Long getCurrentVoteCandidateId(String ipAddress) {
        Long pollId = getCurrentPollId();
        return voteRepository.findByIpAddressAndPollMeta_Id(ipAddress, pollId)
                .map(v -> v.getCandidate().getId())
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public int getRemainingCooldownSeconds(String ipAddress) {
        Long pollId = getCurrentPollId();
        return voteRepository.findByIpAddressAndPollMeta_Id(ipAddress, pollId)
                .map(v -> {
                    long since = Duration.between(v.getCreatedAt(), OffsetDateTime.now()).getSeconds();
                    long remain = COOLDOWN_SECONDS - since;
                    return (int) Math.max(remain, 0);
                })
                .orElse(0);
    }

    /**
     * Returns aggregated results per candidate for admin use only.
     */
    @Transactional(readOnly = true)
    public List<AdminResultDto> getAdminResults() {
        Long pollId = getCurrentPollId();
        return voteRepository.aggregateResultsByPoll(pollId).stream()
                .map(p -> new AdminResultDto(
                        p.getCandidateId(),
                        p.getCandidateName(),
                        p.getTotalVotes()))
                .collect(Collectors.toList());
    }

    private Long getCurrentPollId() {
        return pollMetaRepository.findAll().stream()
                .findFirst()
                .map(PollMeta::getId)
                .orElseThrow(() -> new NotFoundException("No poll available"));
    }
}
