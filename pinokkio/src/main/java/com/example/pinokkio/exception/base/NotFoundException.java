package com.example.pinokkio.exception.base;

import com.example.pinokkio.exception.BaseException;
import java.util.Map;

public class NotFoundException extends BaseException {
    public NotFoundException(String code, String message) {
        super(code, message);
    }

    public NotFoundException(String code, String message, Map<String, String> errors) {
        super(code, message, errors);
    }
}
