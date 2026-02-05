package com.example.voteonline.controller;

import com.example.voteonline.dto.CandidateDto;
import com.example.voteonline.dto.PollSummaryDto;
import com.example.voteonline.dto.VoteRequest;
import com.example.voteonline.model.PollMeta;
import com.example.voteonline.repository.PollMetaRepository;
import com.example.voteonline.service.VoteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Public voting API used by the Vue frontend.
 *
 * - GET /api/vote/candidates : list candidates (id, name)
 * - GET /api/vote/my-vote : get current vote for this IP (if any)
 * - POST /api/vote : cast or update a vote for exactly one candidate
 */
@RestController
@RequestMapping("/api/vote")
@CrossOrigin // allow cross-origin from frontend; can be tightened via configuration if
             // needed
public class VoteController {

    private final VoteService voteService;
    private final PollMetaRepository pollMetaRepository;

    public VoteController(VoteService voteService, PollMetaRepository pollMetaRepository) {
        this.voteService = voteService;
        this.pollMetaRepository = pollMetaRepository;
    }

    @GetMapping("/candidates")
    public List<CandidateDto> getCandidates() {
        return voteService.listCandidates();
    }

    @GetMapping("/polls")
    public List<PollSummaryDto> listPublicPolls() {
        return pollMetaRepository.findAll().stream()
                .map(pm -> new PollSummaryDto(pm.getId(), pm.getTitle()))
                .collect(Collectors.toList());
    }

    @GetMapping("/polls/{id}")
    public ResponseEntity<PollSummaryDto> getPublicPoll(@PathVariable Long id) {
        return pollMetaRepository.findById(id)
                .map(pm -> ResponseEntity.ok(new PollSummaryDto(pm.getId(), pm.getTitle())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/polls/{id}/candidates")
    public ResponseEntity<List<CandidateDto>> getCandidatesByPoll(@PathVariable Long id) {
        if (!pollMetaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(voteService.listCandidatesByPollId(id));
    }

    /**
     * Trả về candidateId mà IP hiện tại đã vote (nếu có).
     * - 200 + { "candidateId": number } nếu đã vote.
     * - 204 No Content nếu chưa vote lần nào.
     */
    @GetMapping("/my-vote")
    public ResponseEntity<Map<String, Long>> getMyVote(HttpServletRequest httpRequest) {
        String ipAddress = extractClientIp(httpRequest);
        Long candidateId = voteService.getCurrentVoteCandidateId(ipAddress);

        if (candidateId == null) {
            return ResponseEntity.noContent().build();
        }

        Map<String, Long> body = new HashMap<>();
        body.put("candidateId", candidateId);
        body.put("cooldownRemainingSeconds", (long) voteService.getRemainingCooldownSeconds(ipAddress));
        return ResponseEntity.ok(body);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> vote(@Valid @RequestBody VoteRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = extractClientIp(httpRequest);
        int remaining = voteService.getRemainingCooldownSeconds(ipAddress);
        if (remaining > 0) {
            Map<String, String> body = new HashMap<>();
            body.put("message", "Please wait " + remaining + " seconds before changing vote.");
            body.put("cooldownRemainingSeconds", String.valueOf(remaining));
            return ResponseEntity.status(429).body(body);
        }
        voteService.castVote(request.getCandidateId(), ipAddress);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Vote submitted successfully.");
        response.put("cooldownSeconds", "10");
        return ResponseEntity.ok(response);
    }

    /**
     * Extract the client's IP address. Behind a reverse proxy, X-Forwarded-For can
     * be used.
     */
    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // Take the first value before comma if multiple IPs are present
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
