package com.example.voteonline.controller;

import com.example.voteonline.dto.CandidateDto;
import com.example.voteonline.dto.VoteRequest;
import com.example.voteonline.service.VoteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Public voting API used by the Vue frontend.
 *
 * - GET /api/vote/candidates : list candidates (id, name)
 * - GET /api/vote/my-vote    : get current vote for this IP (if any)
 * - POST /api/vote           : cast or update a vote for exactly one candidate
 */
@RestController
@RequestMapping("/api/vote")
@CrossOrigin // allow cross-origin from frontend; can be tightened via configuration if needed
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @GetMapping("/candidates")
    public List<CandidateDto> getCandidates() {
        return voteService.listCandidates();
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
        return ResponseEntity.ok(body);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> vote(@Valid @RequestBody VoteRequest request,
                                                    HttpServletRequest httpRequest) {
        String ipAddress = extractClientIp(httpRequest);
        voteService.castVote(request.getCandidateId(), ipAddress);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Vote submitted successfully.");
        return ResponseEntity.ok(response);
    }

    /**
     * Extract the client's IP address. Behind a reverse proxy, X-Forwarded-For can be used.
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

