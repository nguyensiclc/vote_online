package com.example.voteonline.controller;

import com.example.voteonline.dto.AdminResultDto;
import com.example.voteonline.dto.CandidateDto;
import com.example.voteonline.dto.PollSummaryDto;
import com.example.voteonline.dto.PollConfigDto;
import com.example.voteonline.model.Candidate;
import com.example.voteonline.model.PollMeta;
import com.example.voteonline.repository.CandidateRepository;
import com.example.voteonline.repository.PollMetaRepository;
import com.example.voteonline.repository.VoteRepository;
import com.example.voteonline.service.VoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin-only API:
 * - Xem kết quả bình chọn
 * - Quản lý chủ đề & danh sách option bình chọn (đơn giản)
 * (hiện tại hệ thống chỉ hỗ trợ 1 chủ đề hoạt động tại 1 thời điểm).
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final VoteService voteService;
    private final CandidateRepository candidateRepository;
    private final VoteRepository voteRepository;
    private final PollMetaRepository pollMetaRepository;

    public AdminController(VoteService voteService,
            CandidateRepository candidateRepository,
            VoteRepository voteRepository,
            PollMetaRepository pollMetaRepository) {
        this.voteService = voteService;
        this.candidateRepository = candidateRepository;
        this.voteRepository = voteRepository;
        this.pollMetaRepository = pollMetaRepository;
    }

    @GetMapping("/results")
    public List<AdminResultDto> getResults() {
        return voteService.getAdminResults();
    }

    /**
     * Danh sách các chủ đề (polls). Hiện tại chỉ 1 bản ghi.
     */
    @GetMapping("/polls")
    @Transactional(readOnly = true)
    public List<PollSummaryDto> listPolls() {
        return pollMetaRepository.findAll().stream()
                .map(pm -> new PollSummaryDto(pm.getId(), pm.getTitle()))
                .collect(Collectors.toList());
    }

    /**
     * Lấy cấu hình chủ đề bình chọn hiện tại:
     * - title: tiêu đề
     * - options: danh sách option (tên các candidate hiện tại)
     */
    @GetMapping("/poll-config")
    @Transactional(readOnly = true)
    public PollConfigDto getPollConfig() {
        PollMeta meta = pollMetaRepository.findAll().stream().findFirst()
                .orElseGet(() -> new PollMeta("Bình chọn tiết mục văn nghệ"));

        List<String> options = candidateRepository.findByPollMeta_Id(meta.getId()).stream()
                .map(Candidate::getName)
                .collect(Collectors.toList());

        return new PollConfigDto(meta.getTitle(), options);
    }

    /**
     * Cập nhật chủ đề & danh sách option:
     * - Ghi đè title.
     * - Xóa toàn bộ votes & candidates hiện tại.
     * - Tạo lại candidates dựa trên danh sách options mới.
     */
    @PostMapping("/poll-config")
    @Transactional
    public ResponseEntity<Void> updatePollConfig(@Valid @RequestBody PollConfigUpdateRequest request) {
        // Cập nhật / tạo mới meta đang hoạt động (lấy bản ghi đầu tiên)
        PollMeta meta = pollMetaRepository.findAll().stream().findFirst()
                .orElseGet(() -> new PollMeta(request.getTitle()));
        meta.setTitle(request.getTitle());
        pollMetaRepository.save(meta);

        // Đổi chủ đề -> reset votes + candidates của poll hiện tại để tránh dữ liệu sai
        voteRepository.deleteByPollMeta_Id(meta.getId());
        candidateRepository.deleteByPollMeta_Id(meta.getId());

        List<Candidate> candidates = new ArrayList<>();
        for (String opt : request.getOptions()) {
            candidates.add(new Candidate(meta, opt.trim()));
        }
        candidateRepository.saveAll(candidates);

        return ResponseEntity.noContent().build();
    }

    /**
     * Xóa một chủ đề (poll) theo id.
     * Đồng thời xóa toàn bộ candidates và votes liên quan.
     * Lưu ý: hiện hệ thống chỉ hỗ trợ 1 chủ đề hoạt động.
     */
    @DeleteMapping("/polls/{id}")
    @Transactional
    public ResponseEntity<Void> deletePoll(@PathVariable Long id) {
        voteRepository.deleteByPollMeta_Id(id);
        candidateRepository.deleteByPollMeta_Id(id);
        pollMetaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Tạo mới một chủ đề bình chọn.
     * Vì hệ thống hiện chỉ hỗ trợ 1 chủ đề hoạt động, thao tác này sẽ:
     * - Xóa toàn bộ votes & candidates hiện tại
     * - Xóa toàn bộ PollMeta cũ
     * - Tạo PollMeta mới với title được cung cấp
     */
    @PostMapping("/polls")
    @Transactional
    public ResponseEntity<PollSummaryDto> createPoll(@RequestBody PollCreateRequest request) {
        String title = request != null && request.getTitle() != null && !request.getTitle().isBlank()
                ? request.getTitle().trim()
                : "Bình chọn mới";
        PollMeta meta = pollMetaRepository.save(new PollMeta(title));
        return ResponseEntity.status(201).body(new PollSummaryDto(meta.getId(), meta.getTitle()));
    }

    /**
     * Lấy cấu hình của một chủ đề theo id.
     */
    @GetMapping("/polls/{id}/config")
    @Transactional(readOnly = true)
    public ResponseEntity<PollConfigDto> getPollConfigById(@PathVariable Long id) {
        PollMeta meta = pollMetaRepository.findById(id).orElse(null);
        if (meta == null) {
            return ResponseEntity.notFound().build();
        }
        List<String> options = candidateRepository.findByPollMeta_Id(meta.getId()).stream()
                .map(Candidate::getName)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new PollConfigDto(meta.getTitle(), options));
    }

    /**
     * Cập nhật title và danh sách option cho một chủ đề theo id.
     * Sẽ xóa toàn bộ votes và candidates thuộc chủ đề đó rồi tạo lại theo options
     * mới.
     */
    @PostMapping("/polls/{id}/config")
    @Transactional
    public ResponseEntity<Void> updatePollConfigById(@PathVariable Long id,
            @Valid @RequestBody PollConfigUpdateRequest request) {
        PollMeta meta = pollMetaRepository.findById(id).orElse(null);
        if (meta == null) {
            return ResponseEntity.notFound().build();
        }
        meta.setTitle(request.getTitle());
        pollMetaRepository.save(meta);

        voteRepository.deleteByCandidate_PollMeta_Id(meta.getId());
        candidateRepository.deleteByPollMeta_Id(meta.getId());

        List<Candidate> candidates = new ArrayList<>();
        for (String opt : request.getOptions()) {
            candidates.add(new Candidate(meta, opt.trim()));
        }
        candidateRepository.saveAll(candidates);
        return ResponseEntity.noContent().build();
    }

    /**
     * Quản lý option (candidates) theo từng thao tác đơn lẻ.
     */
    @GetMapping("/candidates")
    @Transactional(readOnly = true)
    public List<CandidateDto> listCandidatesAdmin() {
        Long pollId = pollMetaRepository.findAll().stream().findFirst().map(PollMeta::getId).orElse(null);
        return (pollId == null ? candidateRepository.findAll() : candidateRepository.findByPollMeta_Id(pollId)).stream()
                .map(c -> new CandidateDto(c.getId(), c.getName()))
                .collect(Collectors.toList());
    }

    @PostMapping("/candidates")
    @Transactional
    public CandidateDto createCandidate(@RequestBody @NotBlank String name) {
        PollMeta meta = pollMetaRepository.findAll().stream().findFirst()
                .orElseGet(() -> pollMetaRepository.save(new PollMeta("Bình chọn")));
        Candidate c = candidateRepository.save(new Candidate(meta, name.trim()));
        return new CandidateDto(c.getId(), c.getName());
    }

    @PutMapping("/candidates/{id}")
    @Transactional
    public ResponseEntity<Void> renameCandidate(@PathVariable Long id,
            @RequestBody @NotBlank String name) {
        Candidate c = candidateRepository.findById(id).orElse(null);
        if (c == null) {
            return ResponseEntity.notFound().build();
        }
        c.setName(name.trim());
        candidateRepository.save(c);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/candidates/{id}")
    @Transactional
    public ResponseEntity<Void> deleteCandidate(@PathVariable Long id) {
        // Xóa votes trước để tránh lỗi ràng buộc FK
        voteRepository.deleteByCandidate_Id(id);
        candidateRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Request body để cập nhật cấu hình chủ đề bình chọn.
     */
    public static class PollConfigUpdateRequest {

        @NotBlank
        private String title;

        @NotEmpty
        private List<@NotBlank String> options;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<String> getOptions() {
            return options;
        }

        public void setOptions(List<String> options) {
            this.options = options;
        }
    }

    /**
     * Request body tạo chủ đề mới (đơn giản).
     */
    public static class PollCreateRequest {
        private String title;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
