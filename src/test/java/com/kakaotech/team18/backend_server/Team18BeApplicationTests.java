package com.kakaotech.team18.backend_server;

import com.kakaotech.team18.backend_server.domain.email.eventListener.ApplicationNotificationListener;
import com.kakaotech.team18.backend_server.domain.email.sender.SmtpEmailSender;
import com.kakaotech.team18.backend_server.domain.email.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
@SpringBootTest
class Team18BeApplicationTests {

	//자동으로 JavaMailSender bean을 생성하려해서 추가함
	@MockitoBean
	private ApplicationNotificationListener applicationNotificationListener;
	@MockitoBean private JavaMailSender javaMailSender;
	@MockitoBean private SmtpEmailSender smtpEmailSender;
	@MockitoBean private EmailService emailService;


	@Test
	void contextLoads() {
	}

}
