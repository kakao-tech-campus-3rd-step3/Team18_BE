SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE `answer` (
  `answer_id` bigint NOT NULL AUTO_INCREMENT,
  `application_id` bigint NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `form_question_id` bigint NOT NULL,
  `last_modified_at` datetime(6) DEFAULT NULL,
  `answer` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`answer_id`),
  KEY `FKitljjo4w2o0qj6rmuv9qtujyq` (`application_id`),
  KEY `FK7kadfagesegt560acavi83r89` (`form_question_id`),
  CONSTRAINT `FK7kadfagesegt560acavi83r89` FOREIGN KEY (`form_question_id`) REFERENCES `form_question` (`form_question_id`),
  CONSTRAINT `FKitljjo4w2o0qj6rmuv9qtujyq` FOREIGN KEY (`application_id`) REFERENCES `application` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `application` (
  `average_rating` double NOT NULL,
  `application_id` bigint NOT NULL AUTO_INCREMENT,
  `club_apply_form_id` bigint NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `last_modified_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `stage` enum('FINAL','INTERVIEW') COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('APPROVED','PENDING','REJECTED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `interview_date` date DEFAULT NULL,
  `interview_time` time DEFAULT NULL,
  PRIMARY KEY (`application_id`),
  KEY `FKqylpo68i4fse8sm80pu3wn0su` (`club_apply_form_id`),
  KEY `FKawte0mbtubellxed1dvpoxhdj` (`user_id`),
  CONSTRAINT `FKawte0mbtubellxed1dvpoxhdj` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FKqylpo68i4fse8sm80pu3wn0su` FOREIGN KEY (`club_apply_form_id`) REFERENCES `club_apply_form` (`club_apply_form_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `club` (
  `club_id` bigint NOT NULL AUTO_INCREMENT,
  `club_introduction_id` bigint DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `last_modified_at` datetime(6) DEFAULT NULL,
  `recruit_end` datetime(6) DEFAULT NULL,
  `recruit_start` datetime(6) DEFAULT NULL,
  `caution` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `club_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `location` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `regular_meeting_info` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `short_introduction` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category` enum('LITERATURE','RELIGION','SPORTS','STUDY','VOLUNTEER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_interview_required` tinyint(1) NOT NULL DEFAULT '0',
  `interview_start_date` datetime DEFAULT NULL,
  `interview_end_date` datetime DEFAULT NULL,
  `interview_start_time` time DEFAULT NULL,
  `interview_end_time` time DEFAULT NULL,
  PRIMARY KEY (`club_id`),
  UNIQUE KEY `UKbpsde1igbj5ggar8gcnibkk77` (`club_name`),
  UNIQUE KEY `UKgqdl6d03elrcmo7vnb3nnaihc` (`club_introduction_id`),
  CONSTRAINT `FKq1bk2kcu6nt6li7snlem1412n` FOREIGN KEY (`club_introduction_id`) REFERENCES `club_introduction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `club_apply_form` (
  `club_apply_form_id` bigint NOT NULL AUTO_INCREMENT,
  `club_id` bigint NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `last_modified_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `final_message` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `interview_message` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`club_apply_form_id`),
  UNIQUE KEY `UK5tc03iukrsp7our7eqxhfxu8y` (`club_id`),
  CONSTRAINT `FK5ibyg6b5uvo83we1ttdscubfc` FOREIGN KEY (`club_id`) REFERENCES `club` (`club_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `club_image` (
  `club_introduction_id` bigint NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `last_modified_at` datetime(6) DEFAULT NULL,
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKaghr6i05c2dokxtlv22fvj2oo` (`club_introduction_id`),
  CONSTRAINT `FKaghr6i05c2dokxtlv22fvj2oo` FOREIGN KEY (`club_introduction_id`) REFERENCES `club_introduction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `club_introduction` (
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `last_modified_at` datetime(6) DEFAULT NULL,
  `activities` text COLLATE utf8mb4_unicode_ci,
  `ideal` text COLLATE utf8mb4_unicode_ci,
  `overview` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `club_member` (
  `application_id` bigint DEFAULT NULL,
  `club_id` bigint NOT NULL,
  `club_member_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `last_modified_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `active_status` enum('ACTIVE','INACTIVE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `club_role` enum('APPLICANT','CLUB_ADMIN','CLUB_EXECUTIVE','CLUB_MEMBER','SYSTEM_ADMIN') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`club_member_id`),
  UNIQUE KEY `UKl5kcssyclpl7otv2490ce1ghq` (`application_id`),
  KEY `FKf6tl19ih8acrmheidn4xos2tx` (`club_id`),
  KEY `FKbkak0q9hvao01xs5gem9h52tf` (`user_id`),
  CONSTRAINT `FKbkak0q9hvao01xs5gem9h52tf` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FKf6tl19ih8acrmheidn4xos2tx` FOREIGN KEY (`club_id`) REFERENCES `club` (`club_id`),
  CONSTRAINT `FKobdnd29ix5ht0hwxhklyyoee5` FOREIGN KEY (`application_id`) REFERENCES `application` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `club_member_profile` (
  `club_member_profile_id` bigint NOT NULL AUTO_INCREMENT,
  `club_member_id` bigint NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `student_id` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone_number` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `college` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `department` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `academic_status` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `join_date` date NOT NULL,
  `role` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `last_modified_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`club_member_profile_id`),
  UNIQUE KEY `club_member_id` (`club_member_id`),
  CONSTRAINT `fk_club_member_profile_club_member` FOREIGN KEY (`club_member_id`) REFERENCES `club_member` (`club_member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `club_review` (
  `club_id` bigint DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `last_modified_at` datetime(6) DEFAULT NULL,
  `content` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `writer` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5jb0tx5o222iqbhp1q5pgy724` (`club_id`),
  CONSTRAINT `FK5jb0tx5o222iqbhp1q5pgy724` FOREIGN KEY (`club_id`) REFERENCES `club` (`club_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `comment` (
  `rating` double NOT NULL,
  `application_id` bigint NOT NULL,
  `comment_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `last_modified_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `content` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`comment_id`),
  KEY `FKsr2gt5xva5k4esv9ck6wqisyt` (`application_id`),
  KEY `FKqm52p1v3o13hy268he0wcngr5` (`user_id`),
  CONSTRAINT `FKqm52p1v3o13hy268he0wcngr5` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FKsr2gt5xva5k4esv9ck6wqisyt` FOREIGN KEY (`application_id`) REFERENCES `application` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `file` (
  `created_at` datetime(6) DEFAULT NULL,
  `file_id` bigint NOT NULL AUTO_INCREMENT,
  `last_modified_at` datetime(6) DEFAULT NULL,
  `notice_id` bigint NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `object_uri` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`file_id`),
  KEY `FK9sue39n4ky49ha47ujunh07b4` (`notice_id`),
  CONSTRAINT `FK9sue39n4ky49ha47ujunh07b4` FOREIGN KEY (`notice_id`) REFERENCES `notice` (`notice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `form_question` (
  `is_required` bit(1) NOT NULL,
  `club_apply_form_id` bigint NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `display_order` bigint NOT NULL,
  `form_question_id` bigint NOT NULL AUTO_INCREMENT,
  `last_modified_at` datetime(6) DEFAULT NULL,
  `options` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `question` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `field_type` enum('CHECKBOX','RADIO','TEXT','TIME_SLOT') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`form_question_id`),
  KEY `FKje28bthta5ex6rpk569q3tyov` (`club_apply_form_id`),
  CONSTRAINT `FKje28bthta5ex6rpk569q3tyov` FOREIGN KEY (`club_apply_form_id`) REFERENCES `club_apply_form` (`club_apply_form_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `interview_preference` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `application_id` bigint NOT NULL,
  `date` date NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_interview_preference_application` (`application_id`),
  CONSTRAINT `fk_interview_preference_application` FOREIGN KEY (`application_id`) REFERENCES `application` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `interview_preference_time` (
  `interview_preference_id` bigint NOT NULL,
  `time` time NOT NULL,
  KEY `fk_interview_preference_time_preference` (`interview_preference_id`),
  CONSTRAINT `fk_interview_preference_time_preference` FOREIGN KEY (`interview_preference_id`) REFERENCES `interview_preference` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `notice` (
  `is_alive` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `last_modified_at` datetime(6) DEFAULT NULL,
  `notice_id` bigint NOT NULL AUTO_INCREMENT,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` text COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`notice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `time_slot_options` (
  `end_time` time(6) DEFAULT NULL,
  `start_time` time(6) DEFAULT NULL,
  `form_question_id` bigint NOT NULL,
  `date` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  KEY `FK2x2l5tgfi9rr8hs8gmqworv02` (`form_question_id`),
  CONSTRAINT `FK2x2l5tgfi9rr8hs8gmqworv02` FOREIGN KEY (`form_question_id`) REFERENCES `form_question` (`form_question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `users` (
  `created_at` datetime(6) DEFAULT NULL,
  `kakao_id` bigint DEFAULT NULL,
  `last_modified_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `department` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone_number` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `student_id` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UK9q63snka3mdh91as4io72espi` (`phone_number`),
  UNIQUE KEY `UKqh3otyipv2k9hqte4a1abcyhq` (`student_id`),
  UNIQUE KEY `UKk4ycaj27putgcujmehwbsrmmc` (`kakao_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
