package com.example.voteonline.repository;

import com.example.voteonline.model.PollMeta;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository cho PollMeta.
 * Ứng dụng hiện chỉ dùng một bản ghi duy nhất.
 */
public interface PollMetaRepository extends JpaRepository<PollMeta, Long> {
}

