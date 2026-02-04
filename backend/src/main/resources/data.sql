-- Dữ liệu cho 1 cuộc bình chọn với đúng 3 tiết mục văn nghệ: 1, 2, 3
-- Xóa dữ liệu cũ (nếu có) để tránh trùng lặp khi khởi động lại
DELETE FROM votes;
DELETE FROM candidates;

INSERT INTO candidates (id, name) VALUES (1, 'Tiết mục văn nghệ số 1');
INSERT INTO candidates (id, name) VALUES (2, 'Tiết mục văn nghệ số 2');
INSERT INTO candidates (id, name) VALUES (3, 'Tiết mục văn nghệ số 3');