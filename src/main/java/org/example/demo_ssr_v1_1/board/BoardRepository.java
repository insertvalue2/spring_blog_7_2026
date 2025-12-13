package org.example.demo_ssr_v1_1.board;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 게시글 Repository 인터페이스
 * 
 * 핵심 개념:
 * 1. JpaRepository<Entity, ID타입>: Spring Data JPA가 제공하는 인터페이스
 *    - Entity: 엔티티 클래스 (Board)
 *    - ID타입: 엔티티의 기본키 타입 (Long)
 * 
 * 2. 자동 제공 메서드 (별도 구현 없이 사용 가능):
 *    - save(T entity): 엔티티 저장 (INSERT 또는 UPDATE)
 *    - findById(ID id): ID로 엔티티 조회 (Optional<T> 반환)
 *    - findAll(): 모든 엔티티 조회
 *    - deleteById(ID id): ID로 엔티티 삭제
 *    - count(): 전체 개수 조회
 *    - existsById(ID id): ID 존재 여부 확인
 * 
 * 3. 쿼리 메서드 (Query Methods):
 *    - 메서드 이름만으로 쿼리를 자동 생성
 *    - Spring Data JPA가 메서드 이름을 분석하여 SQL 쿼리 생성
 *    - 예: findAllByOrderByCreatedAtDesc() 
 *      -> "SELECT * FROM board_tb ORDER BY created_at DESC"
 * 
 * 4. @Repository: Spring이 이 인터페이스를 스캔하여 프록시 객체를 자동 생성
 *    - 구현체는 Spring Data JPA가 런타임에 자동 생성
 *    - @Repository 생략 가능 (JpaRepository 상속 시 자동 인식)
 *    - 하지만 명시적으로 작성하는 것이 좋음 (의도 명확화)
 * 
 * 5. Repository 네이밍 규칙:
 *    - 일반적으로 "Entity명 + Repository" 형태
 *    - 예: BoardRepository, UserRepository
 *    - BoardPersistRepository → BoardRepository로 변경 (표준 네이밍)
 */
@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    
    /**
     * 게시글 전체 조회 (생성일 기준 내림차순 정렬)
     * 
     * 쿼리 메서드 네이밍 규칙:
     * - findAll: 전체 조회
     * - By: 조건 시작 (여기서는 조건 없음)
     * - OrderBy: 정렬 시작
     * - CreatedAt: 정렬할 필드명 (엔티티의 필드명과 일치해야 함)
     * - Desc: 내림차순 (Asc는 오름차순)
     * 
     * 자동 생성되는 SQL:
     * SELECT * FROM board_tb ORDER BY created_at DESC
     * 
     * 사용 예시:
     * List<Board> boards = boardRepository.findAllByOrderByCreatedAtDesc();
     */
    List<Board> findAllByOrderByCreatedAtDesc();
    
    /**
     * JpaRepository에서 자동 제공되는 메서드들:
     * 
     * 1. save(Board board): 
     *    - ID가 null이면 INSERT, 있으면 UPDATE (더티 체킹)
     *    - 트랜잭션이 끝나면 자동으로 DB에 반영
     *    - 사용 예: boardRepository.save(board);
     * 
     * 2. Optional<Board> findById(Long id):
     *    - ID로 엔티티 조회
     *    - Optional로 반환하여 null 안전성 보장
     *    - 사용 예: boardRepository.findById(id).orElseThrow(...)
     * 
     * 3. void deleteById(Long id):
     *    - ID로 엔티티 삭제
     *    - 엔티티가 없으면 예외 발생 (EmptyResultDataAccessException)
     *    - 사용 예: boardRepository.deleteById(id);
     * 
     * 4. List<Board> findAll():
     *    - 모든 엔티티 조회 (정렬 없음)
     *    - 사용 예: List<Board> boards = boardRepository.findAll();
     * 
     * 5. long count():
     *    - 전체 엔티티 개수 조회
     *    - 사용 예: long count = boardRepository.count();
     * 
     * 6. boolean existsById(Long id):
     *    - ID 존재 여부 확인
     *    - 사용 예: boolean exists = boardRepository.existsById(id);
     * 
     * 더티 체킹 (Dirty Checking):
     * - JPA는 영속성 컨텍스트(Persistence Context)에 있는 엔티티의 변경을 자동 감지
     * - 트랜잭션이 끝날 때 변경된 필드만 자동으로 UPDATE 쿼리 실행
     * - 개발자가 직접 UPDATE 쿼리를 작성할 필요 없음
     * 
     * 예시:
     * @Transactional
     * public void updateBoard(Long id) {
     *     Board board = boardRepository.findById(id).orElseThrow(...);
     *     board.setTitle("새 제목"); // 필드 값 변경
     *     // 트랜잭션 종료 시 자동으로 UPDATE 쿼리 실행 (더티 체킹)
     * }
     */
}

