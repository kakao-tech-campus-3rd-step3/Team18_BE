package com.kakaotech.team18.backend_server.global.config;

import com.kakaotech.team18.backend_server.domain.application.repository.ApplicationRepository;
import com.kakaotech.team18.backend_server.domain.club.repository.ClubRepository;
import com.kakaotech.team18.backend_server.domain.user.repository.UserRepository;
import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 비즈니스 메트릭 설정
 *
 * Prometheus/Grafana에서 수집할 커스텀 비즈니스 메트릭을 정의합니다.
 *
 * 메트릭 종류:
 * - dongarium_users_total: 전체 사용자 수
 * - dongarium_clubs_total: 전체 동아리 수
 * - dongarium_applications_total: 전체 지원서 수
 *
 * @Timed, @Counted 어노테이션을 통한 메서드 레벨 메트릭도 지원합니다.
 *
 * 참고: https://toss.tech/article/how-to-work-health-check-in-spring-boot-actuator
 *       https://www.catsriding.com/posts/custom-metrics-monitoring-in-spring-using-prometheus-and-grafana
 */
@Slf4j
@Configuration
public class MetricsConfig {

    /**
     * @Timed 어노테이션 지원을 위한 Aspect
     * 메서드 실행 시간을 자동으로 측정합니다.
     *
     * 사용 예시:
     * @Timed(value = "dongarium.club.create", description = "Time to create a club")
     * public Club createClub(...) { ... }
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry meterRegistry) {
        return new TimedAspect(meterRegistry);
    }

    /**
     * @Counted 어노테이션 지원을 위한 Aspect
     * 메서드 호출 횟수를 자동으로 측정합니다.
     *
     * 사용 예시:
     * @Counted(value = "dongarium.user.signup", description = "User signup count")
     * public User signup(...) { ... }
     */
    @Bean
    public CountedAspect countedAspect(MeterRegistry meterRegistry) {
        return new CountedAspect(meterRegistry);
    }

    /**
     * 비즈니스 Gauge 메트릭 등록
     * 현재 상태를 실시간으로 조회하는 메트릭입니다.
     */
    @Bean
    public MeterBinder businessMetrics(
            UserRepository userRepository,
            ClubRepository clubRepository,
            ApplicationRepository applicationRepository
    ) {
        return registry -> {
            // 전체 사용자 수
            Gauge.builder("dongarium.users.total", userRepository::count)
                    .description("Total number of registered users")
                    .tag("entity", "user")
                    .register(registry);

            // 전체 동아리 수
            Gauge.builder("dongarium.clubs.total", clubRepository::count)
                    .description("Total number of clubs")
                    .tag("entity", "club")
                    .register(registry);

            // 전체 지원서 수
            Gauge.builder("dongarium.applications.total", applicationRepository::count)
                    .description("Total number of applications")
                    .tag("entity", "application")
                    .register(registry);

            log.info("Business metrics registered: dongarium.users.total, dongarium.clubs.total, dongarium.applications.total");
        };
    }
}