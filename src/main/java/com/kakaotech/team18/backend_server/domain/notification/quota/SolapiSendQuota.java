package com.kakaotech.team18.backend_server.domain.notification.quota;

import com.kakaotech.team18.backend_server.domain.notification.sms.PreparedSmsMessage;

public interface SolapiSendQuota {

    void reserve(PreparedSmsMessage message);
}
