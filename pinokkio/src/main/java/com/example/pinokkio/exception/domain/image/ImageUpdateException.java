package com.example.pinokkio.exception.domain.image;

import com.example.pinokkio.exception.base.BadInputException;

import java.util.Map;

/**
 * 400 BAD INPUT
 */
public class ImageUpdateException extends BadInputException {
    public ImageUpdateException() {
        super(
                "UPDATE_IMAGE_01",
                "업데이트 중 오류가 발생했습니다"
        );
    }
}