package com.kakaotech.team18.backend_server.global.converter;

import com.kakaotech.team18.backend_server.domain.application.entity.Status;
import com.kakaotech.team18.backend_server.global.exception.exceptions.StatusNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class StatusConverter implements Converter<String, Status> {

    @Override
    public Status convert(String source) {
        if (!StringUtils.hasText(source)) {
            return null;
        }
        // 한글 라벨과 enum name 둘 다 허용 (대소문자 무시)

        for (Status s : Status.values()) {
            if (s.name().equalsIgnoreCase(source) || s.getText().equals(source)) {
                return s;
            }
        }
        log.warn("Unknown status: {}", source);
        throw new StatusNotFoundException("잘못된 상태값: " + source);
    }
}