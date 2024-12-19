package flab.Linkedlog.controller;

import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.member.LogInRequest;
import flab.Linkedlog.dto.member.SignUpRequest;
import flab.Linkedlog.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping(value = "/signup")
    public ApiResponse<String> createMember(@RequestBody @Validated SignUpRequest signUpRequest) {
        memberService.signUp(signUpRequest);

        return ApiResponse.success(signUpRequest.getUserId());

    }

    @PostMapping(value = "/login")
    public ApiResponse<String> login(@RequestBody @Validated LogInRequest loginRequest) {
        String token = memberService.login(loginRequest);

        return ApiResponse.success(token);

    }

}
