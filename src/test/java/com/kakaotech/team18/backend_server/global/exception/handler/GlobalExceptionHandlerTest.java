package com.kakaotech.team18.backend_server.global.exception.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakaotech.team18.backend_server.global.exception.code.ErrorCode;
import com.kakaotech.team18.backend_server.global.exception.exceptions.CustomException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = GlobalExceptionHandler.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
    }
)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestController.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 테스트를 위한 가짜 컨트롤러
    @RestController
    static class TestController {

        // CustomException (detail 없음)
        @GetMapping("/test/custom-exception-no-detail")
        public void throwCustomExceptionNoDetail() {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // CustomException (detail 있음)
        @GetMapping("/test/custom-exception-with-detail")
        public void throwCustomExceptionWithDetail() {
            throw new CustomException(ErrorCode.USER_NOT_FOUND, "userId: 123");
        }

        // MethodArgumentNotValidException (@Valid 실패)
        @PostMapping("/test/validation-Exception")
        public void validateInput(@Valid @RequestBody TestDto testDto) {
            // 이 메소드는 요청이 유효하면 아무것도 하지 않음
        }

        // 처리되지 않은 일반 예외
        @GetMapping("/test/unhandled-exception")
        public void throwUnhandledException() {
            throw new RuntimeException("예상치 못한 에러 발생");
        }
    }

    // MethodArgumentNotValidException (@Valid 실패) 테스트용 DTO
    @Data
    @NoArgsConstructor
    static class TestDto {

        @NotBlank(message = "이름은 비워둘 수 없습니다.")
        private String name;
    }

    @Test
    @DisplayName("CustomException 처리 테스트 (detail 없음)")
    void handleCustomException_noDetail_test() throws Exception {
        // given
        final String url = "/test/custom-exception-no-detail";
        final ErrorCode errorCode = ErrorCode.USER_ALREADY_EXISTS;

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().is(errorCode.getHttpStatus().value()))
                .andExpect(jsonPath("$.errorCode").value(errorCode.getCode()))
                .andExpect(jsonPath("$.message").value(errorCode.getMessage()))
                .andExpect(jsonPath("$.detail").isEmpty()); // detail이 null 인지 확인
    }

    @Test
    @DisplayName("CustomException 처리 테스트 (detail 있음)")
    void handleCustomException_withDetail_test() throws Exception {
        // given
        final String url = "/test/custom-exception-with-detail";
        final ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;
        final String detail = "userId: 123";

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().is(errorCode.getHttpStatus().value()))
                .andExpect(jsonPath("$.errorCode").value(errorCode.getCode()))
                .andExpect(jsonPath("$.message").value(errorCode.getMessage()))
                .andExpect(jsonPath("$.detail").value(detail));
    }

    @Test
    @DisplayName("@Valid 유효성 검사 실패 테스트")
    void handleMethodArgumentNotValidException_test() throws Exception {
        // given
        final String url = "/test/validation-Exception";
        final TestDto invalidDto = new TestDto(); // name을 null로 설정하여 @NotBlank 위반
        final String requestBody = objectMapper.writeValueAsString(invalidDto);
        final ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

        // when
        ResultActions resultActions = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        resultActions.andExpect(status().is(errorCode.getHttpStatus().value()))
                .andExpect(jsonPath("$.errorCode").value(errorCode.getCode()))
                .andExpect(jsonPath("$.message").value(errorCode.getMessage()))
                .andExpect(jsonPath("$.detail").value("name: 이름은 비워둘 수 없습니다."));
    }

    @Test
    @DisplayName("처리되지 않은 일반 예외 테스트")
    void handleException_test() throws Exception {
        // given
        final String url = "/test/unhandled-exception";
        final ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        // when
        ResultActions resultActions = mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().is(errorCode.getHttpStatus().value()))
                .andExpect(jsonPath("$.errorCode").value(errorCode.getCode()))
                .andExpect(jsonPath("$.message").value(errorCode.getMessage()));
    }
}
