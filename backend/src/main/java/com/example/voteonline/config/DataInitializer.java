package com.example.voteonline.config;

import com.example.voteonline.model.Candidate;
import com.example.voteonline.repository.CandidateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Khởi tạo dữ liệu mặc định cho cuộc bình chọn khi ứng dụng BE start.
 *
 * - Khi khởi chạy, nếu bảng candidates đang trống thì tự động insert
 *   3 tiết mục văn nghệ số 1, 2, 3.
 * - Nếu đã có dữ liệu rồi thì giữ nguyên, không ghi đè.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CandidateRepository candidateRepository;

    public DataInitializer(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        long count = candidateRepository.count();
        if (count > 0) {
            log.info("Candidates already exist ({} records). Skip init.", count);
            return;
        }

        List<Candidate> defaults = List.of(
                new Candidate("Tiết mục văn nghệ số 1"),
                new Candidate("Tiết mục văn nghệ số 2"),
                new Candidate("Tiết mục văn nghệ số 3")
        );

        candidateRepository.saveAll(defaults);
        log.info("Initialized default candidates for voting (3 records).");
    }
}

