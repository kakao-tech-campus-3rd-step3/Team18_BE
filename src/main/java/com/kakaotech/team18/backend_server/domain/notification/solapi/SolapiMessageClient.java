package com.kakaotech.team18.backend_server.domain.notification.solapi;

public interface SolapiMessageClient {

    SolapiSendResponse send(SolapiSmsRequest request);
}
