package com.example.voteonline.controller;

import com.example.voteonline.dto.AdminResultDto;
import com.example.voteonline.service.VoteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin-only API to retrieve aggregated vote results.
 * Protected via HTTP Basic authentication.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final VoteService voteService;

    public AdminController(VoteService voteService) {
        this.voteService = voteService;
    }

    @GetMapping("/results")
    public List<AdminResultDto> getResults() {
        return voteService.getAdminResults();
    }
}

