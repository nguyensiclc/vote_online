package com.example.voteonline.config;

import com.example.voteonline.model.Candidate;
import com.example.voteonline.model.PollMeta;
import com.example.voteonline.repository.CandidateRepository;
import com.example.voteonline.repository.PollMetaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Khởi tạo dữ liệu mặc định cho cuộc bình chọn khi ứng dụng BE start.
 *
 * - Nếu chưa có meta chủ đề bình chọn thì khởi tạo một bản ghi mặc định.
 * - Nếu bảng candidates đang trống thì tự động insert
 *   3 tiết mục văn nghệ số 1, 2, 3.
 * - Nếu đã có dữ liệu rồi thì giữ nguyên, không ghi đè.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CandidateRepository candidateRepository;
    private final PollMetaRepository pollMetaRepository;

    public DataInitializer(CandidateRepository candidateRepository,
                           PollMetaRepository pollMetaRepository) {
        this.candidateRepository = candidateRepository;
        this.pollMetaRepository = pollMetaRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Khởi tạo meta chủ đề nếu chưa có
        PollMeta meta = pollMetaRepository.findAll().stream().findFirst().orElse(null);
        if (meta == null) {
            meta = new PollMeta("Bình chọn tiết mục văn nghệ");
            pollMetaRepository.save(meta);
            log.info("Initialized default poll meta.");
        }

        long count = candidateRepository.count();
        if (count > 0) {
            log.info("Candidates already exist ({} records). Skip init.", count);
            return;
        }

        List<Candidate> defaults = List.of(
                new Candidate(meta, "Tiết mục văn nghệ số 1"),
                new Candidate(meta, "Tiết mục văn nghệ số 2"),
                new Candidate(meta, "Tiết mục văn nghệ số 3")
        );

        candidateRepository.saveAll(defaults);
        log.info("Initialized default candidates for voting (3 records).");
    }
}


