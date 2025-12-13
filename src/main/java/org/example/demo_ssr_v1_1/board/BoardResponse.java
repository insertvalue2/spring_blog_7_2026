package org.example.demo_ssr_v1_1.board;

import lombok.Data;
import org.example.demo_ssr_v1_1.utils.MyDateUtil;

/**
 * 게시글 응답 DTO
 * 
 * Open Session in View가 false일 때:
 * - 트랜잭션이 끝나면 세션이 종료되어 LAZY 로딩 불가
 * - Service에서 필요한 데이터를 모두 조회하고 DTO로 변환하여 반환
 * - 엔티티를 직접 반환하지 않고 DTO를 반환하여 계층 간 결합도 감소
 */
public class BoardResponse {

    /**
     * 게시글 목록 응답 DTO
     */
    @Data
    public static class ListDTO {
        private Long id;
        private String title;
        private String username;  // 작성자명
        private String createdAt; // 포맷된 생성일

        public ListDTO(Board board) {
            this.id = board.getId();
            this.title = board.getTitle();
            // JOIN FETCH로 이미 로딩된 user 사용 (추가 쿼리 없음)
            if (board.getUser() != null) {
                this.username = board.getUser().getUsername();
            }
            // 날짜 포맷팅
            if (board.getCreatedAt() != null) {
                this.createdAt = MyDateUtil.timestampFormat(board.getCreatedAt());
            }
        }
    }

    /**
     * 게시글 상세 응답 DTO
     */
    @Data
    public static class DetailDTO {
        private Long id;
        private String title;
        private String content;
        private Long userId;      // 작성자 ID
        private String username;  // 작성자명
        private String createdAt; // 포맷된 생성일

        public DetailDTO(Board board) {
            this.id = board.getId();
            this.title = board.getTitle();
            this.content = board.getContent();
            // JOIN FETCH로 이미 로딩된 user 사용 (추가 쿼리 없음)
            if (board.getUser() != null) {
                this.userId = board.getUser().getId();
                this.username = board.getUser().getUsername();
            }
            // 날짜 포맷팅
            if (board.getCreatedAt() != null) {
                this.createdAt = MyDateUtil.timestampFormat(board.getCreatedAt());
            }
        }
    }

    /**
     * 게시글 수정 화면 응답 DTO
     */
    @Data
    public static class UpdateFormDTO {
        private Long id;
        private String title;
        private String content;

        public UpdateFormDTO(Board board) {
            this.id = board.getId();
            this.title = board.getTitle();
            this.content = board.getContent();
        }
    }
}

