package org.example.demo_ssr_v1_1.user;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

// 엔티티 화면 보고 설계해 보세요.
@NoArgsConstructor
@Data
@Table(name = "user_tb")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;
    private String password;
    private String email;

    @CreationTimestamp
    private Timestamp createdAt;

    @Builder
    public User(Long id, String username, String password, String email, Timestamp createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.createdAt = createdAt;
    }

    // 회원정보 수정 비즈니스 로직 추가
    // 추후 DTO  설계
    public void update(UserRequest.UpdateDTO updateDTO) {
        // 유효성 검사
        updateDTO.validate();
        this.password = updateDTO.getPassword();
        // 더티 체킹 (변경 감지)
        // 트랜잭션이 끝나면 자동으로 update 쿼리 진행
    }

    // 회원정보 소유자 확인 로직
    public boolean isOwner(Long userId) {
        if (this.id == null || userId == null) {
            return false;
        }
        return this.id.equals(userId);
    }

}
