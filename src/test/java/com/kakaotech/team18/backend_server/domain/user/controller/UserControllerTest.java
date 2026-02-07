package com.kakaotech.team18.backend_server.domain.user.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kakaotech.team18.backend_server.domain.user.dto.MyProfileResponseDto;
import com.kakaotech.team18.backend_server.domain.user.dto.UserActivityDto;
import com.kakaotech.team18.backend_server.domain.user.service.UserService;
import com.kakaotech.team18.backend_server.global.config.SecurityConfig;
import com.kakaotech.team18.backend_server.global.config.TestSecurityConfig;
import com.kakaotech.team18.backend_server.global.security.JwtAuthenticationFilter;
import com.kakaotech.team18.backend_server.global.security.WithMockCustomUser;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = UserController.class, excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
})
@Import(TestSecurityConfig.class)
class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private UserService userService;

        @Test
        @DisplayName("My Profile 조회 성공")
        @WithMockCustomUser(userId = 1L)
        void getMyProfile_success() throws Exception {
                // given
                Long userId = 1L;
                MyProfileResponseDto responseDto = new MyProfileResponseDto(
                                userId, "홍길동", "20230001", "컴퓨터공학과", "010-1234-5678", "test@test.com");

                given(userService.getMyProfile(userId)).willReturn(responseDto);

                // when
                ResultActions resultActions = mockMvc.perform(get("/api/users/profile"));

                // then
                resultActions.andExpect(status().isOk())
                                .andExpect(jsonPath("$.userId").value(userId))
                                .andExpect(jsonPath("$.name").value("홍길동"))
                                .andExpect(jsonPath("$.studentId").value("20230001"))
                                .andDo(print());
        }

        @Test
        @DisplayName("My Activities 조회 성공")
        @WithMockCustomUser(userId = 1L)
        void getMyActivities_success() throws Exception {
                // given
                Long userId = 1L;
                UserActivityDto responseDto = new UserActivityDto(
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.emptyList());

                given(userService.getMyActivities(userId)).willReturn(responseDto);

                // when
                ResultActions resultActions = mockMvc.perform(get("/api/users/activities"));

                // then
                resultActions.andExpect(status().isOk())
                                .andExpect(jsonPath("$.reviews").isArray())
                                .andExpect(jsonPath("$.comments").isArray())
                                .andExpect(jsonPath("$.applications").isArray())
                                .andDo(print());
        }
}
