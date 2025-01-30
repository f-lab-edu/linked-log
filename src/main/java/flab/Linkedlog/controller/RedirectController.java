package flab.Linkedlog.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
public class RedirectController {

    @GetMapping("/fail")
    public String redirectToPaymentFail(@RequestParam(required = false) String message,
                                        @RequestParam(required = false) String code) {
        log.info("결제 실패 리다이렉트: message={}, code={}", message, code);
        return "redirect:/front/payment/fail?message=" + (message != null ? message : "") + "&code=" + (code != null ? code : "");
    }

}