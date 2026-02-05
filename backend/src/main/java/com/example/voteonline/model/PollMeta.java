package com.example.voteonline.model;

import jakarta.persistence.*;

/**
 * Lưu meta đơn giản cho cuộc bình chọn hiện tại (chủ đề, tiêu đề...).
 * Ứng dụng hiện tại chỉ dùng một chủ đề bình chọn tại một thời điểm.
 */
@Entity
@Table(name = "poll_meta")
public class PollMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    public PollMeta() {
    }

    public PollMeta(String title) {
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

