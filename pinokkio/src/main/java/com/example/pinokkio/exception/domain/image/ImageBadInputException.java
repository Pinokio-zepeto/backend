package com.example.pinokkio.exception.domain.image;

import com.example.pinokkio.exception.base.BadInputException;
import java.util.Map;

/**
 * 400 BAD INPUT
 */
public class ImageBadInputException extends BadInputException {
    public ImageBadInputException(String filePath) {
        super(
                "BAD_INPUT_IMAGE_01",
                "이미지 형식이 잘못되었습니다.",
                Map.of("filePath", filePath)
        );
    }
}