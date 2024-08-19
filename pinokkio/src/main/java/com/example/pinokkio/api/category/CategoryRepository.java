package com.example.pinokkio.api.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    /**
     * 특정 POS ID와 관련된 모든 카테고리를 조회합니다.
     *
     * @param posId POS ID
     * @return 특정 POS ID와 관련된 모든 카테고리 리스트
     */
    @Query("SELECT c " +
            "FROM Category c " +
            "WHERE c.pos.id = :posId")
    List<Category> findAllByPosId(@Param("posId") UUID posId);

    /**
     * 특정 POS ID와 카테고리 ID를 가진 카테고리를 삭제합니다.
     *
     * @param posId      POS ID
     * @param categoryId 카테고리 ID
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Category c " +
            "WHERE c.id = :categoryId " +
            "AND c.pos.id = :posId")
    void deleteByPosIdAndCategoryId(@Param("posId") UUID posId, @Param("categoryId") UUID categoryId);

    /**
     * 특정 POS ID와 카테고리 ID를 가진 카테고리가 존재하는지 확인합니다.
     *
     * @param posId      POS ID
     * @param categoryId 카테고리 ID
     * @return 카테고리 존재 여부
     */
    @Query("SELECT COUNT(c) > 0 " +
            "FROM Category c " +
            "WHERE c.pos.id = :posId " +
            "AND c.id = :categoryId")
    boolean existsByPosIdAndCategoryId(@Param("posId") UUID posId, @Param("categoryId") UUID categoryId);

    /**
     * 특정 카테고리 ID와 POS ID를 가진 카테고리를 조회합니다.
     *
     * @param categoryId 카테고리 ID
     * @param posId      POS ID
     * @return 카테고리(Optional)
     */
    @Query("SELECT c " +
            "FROM Category c " +
            "WHERE c.id = :categoryId " +
            "AND c.pos.id = :posId")
    Optional<Category> findByCategoryIdAndPosId(@Param("categoryId") UUID categoryId, @Param("posId") UUID posId);
}