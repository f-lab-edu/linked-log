package flab.Linkedlog.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class TossPaymentConfig {
    @Value("${payment.toss.base-url}")
    private String tossBaseUrl;

    @Value("${payment.toss.test-client-key}")
    private String testClientKey;

    @Value("${payment.toss.test-secret-key}")
    private String testSecretKey;

}