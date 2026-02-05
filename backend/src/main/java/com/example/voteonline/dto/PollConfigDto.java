package com.example.voteonline.dto;

import java.util.List;

/**
 * DTO quản lý cấu hình một chủ đề bình chọn:
 * - title: tiêu đề chủ đề
 * - options: danh sách tên các option (tiết mục) hiện có
 */
public class PollConfigDto {

    private String title;
    private List<String> options;

    public PollConfigDto() {
    }

    public PollConfigDto(String title, List<String> options) {
        this.title = title;
        this.options = options;
    }

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

