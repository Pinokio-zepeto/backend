package com.example.pinokkio.api.item;

import com.example.pinokkio.api.category.Category;
import com.example.pinokkio.api.category.CategoryRepository;
import com.example.pinokkio.api.item.dto.request.ItemRequest;
import com.example.pinokkio.api.item.dto.request.UpdateItemRequest;
import com.example.pinokkio.api.item.image.ImageService;
import com.example.pinokkio.api.pos.Pos;
import com.example.pinokkio.api.user.UserService;
import com.example.pinokkio.common.type.IsScreen;
import com.example.pinokkio.common.type.IsSoldOut;
import com.example.pinokkio.common.utils.EntityUtils;
import com.example.pinokkio.exception.base.BadInputException;
import com.example.pinokkio.exception.domain.category.CategoryNotFoundException;
import com.example.pinokkio.exception.domain.image.ImageUpdateException;
import com.example.pinokkio.exception.domain.item.ItemNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    @Value("${default-image}")
    private String DEFAULT_IMAGE_URL;

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final ImageService imageService;
    private final UserService userService;

    /**
     * 특정 포스의 개별 아이템 조회
     */
    public Item getItem(UUID itemId) {
        UUID posId = userService.getCurrentPosId();
        validateItem(itemId, posId);
        return itemRepository
                .findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
    }

    /**
     * 특정 포스의 전체 아이템 조회
     */
    public List<Item> getGroupItems() {
        UUID posId = userService.getCurrentPosId();
        return itemRepository.findAllByPosId(posId);
    }

    /**
     * 특정 포스의 특정 카테고리 전체 아이템 조회
     */
    public List<Item> getGroupItemsByCategory(UUID categoryId) {
        UUID posId = userService.getCurrentPosId();
        return itemRepository.findByCategoryIdAndPosId(categoryId, posId);
    }

    /**
     * 특정 포스의 키워드 기반 아이템 검색
     */
    public List<Item> getGroupItemsByKeyword(String keyword) {
        UUID posId = userService.getCurrentPosId();
        return itemRepository.findItemsByKeyWordAndPosId(keyword, posId);
    }

    /**
     * 특정 포스의 아이템 생성
     */
    @Transactional
    public Item createItem(ItemRequest itemRequest, MultipartFile file) {
        Pos pos = userService.getCurrentPos();

        String imageURL = DEFAULT_IMAGE_URL;
        if (file != null && !file.isEmpty()) {
            imageURL = imageService.uploadImage(file);
        }

        Category findCategory = categoryRepository.findById(itemRequest.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(itemRequest.getCategoryId()));

        Item item = Item.builder()
                .pos(pos)
                .category(findCategory)
                .price(itemRequest.getPrice())
                .amount(itemRequest.getAmount())
                .name(itemRequest.getName())
                .detail(itemRequest.getDetail())
                .itemImage(imageURL).build();
        return itemRepository.save(item);
    }

    /**
     * 특정 포스의 아이템 수정
     */
    @Transactional
    public void updateItem(UUID itemId, UpdateItemRequest updateRequest, MultipartFile file) {
        UUID posId = userService.getCurrentPosId();
        Item item = getAndValidateItem(itemId, posId);
        Category category = getCategory(posId, updateRequest.getCategoryId());

        updateItemDetails(item, updateRequest, category);
        updateItemImage(item, file, updateRequest.getUseExistingImage());
    }

    private void updateItemDetails(Item item, UpdateItemRequest updateRequest, Category category) {

        item.updateCategory(category);
        item.updateAmount(updateRequest.getAmount());
        item.updatePrice(updateRequest.getPrice());
        item.updateName(updateRequest.getName());
        item.updateDetail(updateRequest.getDetail());
        try {
            item.updateIsScreen(IsScreen.valueOf(updateRequest.getIsScreen().toUpperCase()));
            item.updateIsSoldOut(IsSoldOut.valueOf(updateRequest.getIsSoldOut().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BadInputException("BAD_INPUT_001", e.getMessage());
        }
    }

    private void updateItemImage(Item item, MultipartFile file, Boolean useExistingImage) {
        try {
            log.info("[updateItemImage] file: {}", file.getOriginalFilename());
            String fileName = file.getOriginalFilename();

            String currentImageUrl = item.getItemImage();
            String newImageUrl = null;

            // 이미지 유지시 바로 리턴
            if (useExistingImage) {
                return;
            }

            // 새 파일이 제공된 경우에만 이미지 업데이트 진행
            if (fileName != null && !fileName.isEmpty()) {
                // 기존 이미지 삭제
                if (currentImageUrl != null) {
                    try {
//                        imageService.deleteImage(currentImageUrl);
                    } catch (Exception e) {
                        log.warn("기존 이미지 삭제 실패: {}", currentImageUrl, e);
                        // 기존 이미지 삭제 실패를 로그로 남기고 계속 진행
                    }
                }

                // 새 이미지 업로드
                newImageUrl = imageService.uploadImage(file);
            }

            log.info("[updateImage] fileName != null : {}", (fileName != null));
            log.info("[updateImage] fileName.isEmpty() : {}", (fileName.isEmpty()));

            // 아이템 이미지 URL 업데이트
            if (newImageUrl != null) {
                item.updateItemImage(newImageUrl);
            } else if (fileName != null && fileName.isEmpty()) {
                // 파일이 제공되었지만 비어있는 경우, 이미지 제거로 간주해 default 이미지로 대치
                log.info("[updateImage] DEFAULT 값으로 대치");
                item.updateItemImage(DEFAULT_IMAGE_URL);
            }

        } catch (Exception e) {
            log.error("이미지 업데이트 중 오류 발생", e);
            throw new ImageUpdateException();
        }
    }

    /**
     * 특정 포스의 아이템 삭제
     */
    @Transactional
    public void deleteItem(UUID itemId) {
        UUID posId = userService.getCurrentPosId();
        EntityUtils.getEntityById(itemRepository, itemId, ItemNotFoundException::new);
        validateItem(itemId, posId);
        itemRepository.deleteByItemIdAndPosId(itemId, posId);
    }

    @Transactional
    public void toggleScreenStatus(UUID itemId) {
        UUID posId = userService.getCurrentPosId();
        Item item = EntityUtils.getEntityById(itemRepository, itemId, ItemNotFoundException::new);
        validateItem(itemId, posId);
        item.toggleIsScreen();
    }

    @Transactional
    public void toggleSoldOutStatus(UUID itemId) {
        UUID posId = userService.getCurrentPosId();
        Item item = EntityUtils.getEntityById(itemRepository, itemId, ItemNotFoundException::new);
        validateItem(itemId, posId);
        item.toggleIsSoldOut();
    }


    /**
     * 해당 아이템이 입력받은 포스의 아이템인지 검증한다.
     *
     * @param itemId 아이템 식별자
     * @param posId  포스 식별자
     */
    public void validateItem(UUID itemId, UUID posId) {
        if (!itemRepository.existsByItemIdAndPosId(itemId, posId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "아이템이 해당 포스에 존재하지 않습니다.");
        }
    }

    private Item getAndValidateItem(UUID itemId, UUID posId) {
        return itemRepository.findItemByIdAndPosId(itemId, posId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
    }

    private Category getCategory(UUID posId, UUID categoryId) {
        return categoryRepository.findByCategoryIdAndPosId(categoryId, posId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }
}
