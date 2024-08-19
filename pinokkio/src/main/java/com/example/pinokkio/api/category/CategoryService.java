package com.example.pinokkio.api.category;


import com.example.pinokkio.api.category.dto.request.CategoryRequest;
import com.example.pinokkio.api.item.Item;
import com.example.pinokkio.api.item.ItemRepository;
import com.example.pinokkio.api.pos.Pos;
import com.example.pinokkio.api.user.UserService;
import com.example.pinokkio.exception.domain.category.CanDeleteCategoryException;
import com.example.pinokkio.exception.domain.category.CategoryNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;

    /**
     * 특정 포스의 카테고리 목록 조회
     */
    public List<Category> getGroupCategories() {
        UUID posId = userService.getCurrentPosId();
        return categoryRepository.findAllByPosId(posId);
    }

    /**
     * 특정 포스의 카테고리 생성
     */
    @Transactional
    public Category createCategory(String name) {
        Pos pos = userService.getCurrentPos();
        Category category = Category.builder()
                .name(name)
                .pos(pos)
                .build();
        return categoryRepository.save(category);
    }

    /**
     * 특정 포스의 카테고리 삭제
     */
    @Transactional
    public void deleteCategory(UUID categoryId) {
        UUID posId = userService.getCurrentPosId();
        validateCategory(categoryId, posId);
        List<Item> findList = itemRepository.findAllByCategoryId(categoryId);
        if(!ObjectUtils.isEmpty(findList)) {
            throw new CanDeleteCategoryException(categoryId);
        }
        categoryRepository.deleteByPosIdAndCategoryId(posId, categoryId);
        log.info("카테고리 삭제 성공: 카테고리 ID = {}", categoryId);
    }

    /**
     * 특정 포스의 카테고리명 수정
     */
    @Transactional
    public void updateCategory(UUID categoryId, CategoryRequest categoryRequest) {
        UUID posId = userService.getCurrentPosId();
        Category category = categoryRepository.findByCategoryIdAndPosId(categoryId, posId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
        category.updateName(categoryRequest.getName());
        log.info("카테고리 업데이트 성공: 카테고리 ID = {}", categoryId);
    }

    /**
     * 카테고리 검증 함수
     * 해당 카테고리가 입력받은 포스의 카테고리인지 검증한다.
     *
     * @param categoryId 카테고리 식별자
     */
    public void validateCategory(UUID categoryId, UUID posId) {
        if (!categoryRepository.existsByPosIdAndCategoryId(posId, categoryId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "카테고리가 해당 포스에 존재하지 않습니다.");
        }
    }
}