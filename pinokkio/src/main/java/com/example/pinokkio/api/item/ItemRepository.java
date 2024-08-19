package com.example.pinokkio.api.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemRepository extends JpaRepository<Item, UUID> {

    /**
     * 입력받은 카테고리 아이디에 해당하고 특정 posId를 가진 아이템 리스트를 반환한다.
     * @param categoryId 카테고리 아이디
     * @param posId 포스 아이디
     * @return 카테고리 아이디에 해당하고 특정 posId를 가진 아이템 리스트
     */
    @Query("SELECT i " +
            "FROM Item i " +
            "WHERE i.category.id = :categoryId " +
            "AND i.pos.id = :posId")
    List<Item> findByCategoryIdAndPosId(@Param("categoryId") UUID categoryId, @Param("posId") UUID posId);

    /**
     * 검색어를 접두사로 포함하고 특정 posId를 가진 아이템 리스트를 반환한다.
     * @param keyword 입력받는 검색어
     * @param posId 포스 아이디
     * @return 검색어를 포함하고 특정 posId를 가진 아이템 리스트
     */
    @Query("SELECT i " +
            "FROM Item i " +
            "WHERE i.name " +
            "LIKE %:keyword% " +
            "AND i.pos.id = :posId")
    List<Item> findItemsByKeyWordAndPosId(@Param("keyword") String keyword, @Param("posId") UUID posId);

    /**
     * 입력받은 아이템이 입력받은 포스의 것인지 여부를 반환한다.
     * @param itemId 입력받는 아이템 아이디
     * @param posId 입력받는 포스 아이디
     * @return 입력받은 아이템이 입력받은 포스의 것인지 여부
     */
    @Query("SELECT " +
            "CASE WHEN COUNT(i) > 0 THEN TRUE " +
            "ELSE FALSE END " +
            "FROM Item i " +
            "WHERE i.id = :itemId " +
            "AND i.pos.id = :posId")
    boolean existsByItemIdAndPosId(@Param("itemId") UUID itemId, @Param("posId") UUID posId);

    /**
     * 특정 itemId와 posId를 가진 아이템을 삭제한다.
     * @param itemId 삭제할 아이템 아이디
     * @param posId 포스 아이디
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Item i " +
            "WHERE i.id = :itemId " +
            "AND i.pos.id = :posId")
    void deleteByItemIdAndPosId(@Param("itemId") UUID itemId, @Param("posId") UUID posId);


    /**
     * 특정 posId를 가진 모든 아이템 리스트를 반환한다.
     * @param posId 포스 아이디
     * @return 특정 posId를 가진 모든 아이템 리스트
     */
    @Query("SELECT i " +
            "FROM Item i " +
            "WHERE i.pos.id = :posId")
    List<Item> findAllByPosId(@Param("posId") UUID posId);


     /**
      * 입력받은 카테고리 아이디에 해당하는 아이템 리스트를 반환한다.
      * @param categoryId 카테고리 아이디
      * @return 카테고리 아이디에 해당하는 아이템 리스트
     */
    @Query("SELECT i " +
            "FROM Item i " +
            "WHERE i.category.id = :categoryId")
    List<Item> findAllByCategoryId(@Param("categoryId") UUID categoryId);


    Optional<Item> findItemByIdAndPosId(UUID id, UUID posId);
}
