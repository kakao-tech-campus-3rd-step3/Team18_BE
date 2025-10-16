package com.kakaotech.team18.backend_server.domain.email.template;

import java.util.Map;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class EmailTemplateRenderer {

    private final TemplateEngine templateEngine;

    public EmailTemplateRenderer(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String render(String templateName, Map<String, Object> model) {
        Context ctx = new Context();
        model.forEach(ctx::setVariable);
        return templateEngine.process(templateName, ctx);
    }
}
