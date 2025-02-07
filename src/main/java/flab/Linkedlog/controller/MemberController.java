package flab.Linkedlog.controller;

import flab.Linkedlog.config.CustomUserDetails;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.member.LogInRequest;
import flab.Linkedlog.dto.member.MyPageResponse;
import flab.Linkedlog.dto.member.SignUpRequest;
import flab.Linkedlog.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "회원가입")
    public ApiResponse<String> createMember(
            @RequestPart(value = "signUpRequest", required = true)
            @Valid SignUpRequest signUpRequest,

            @RequestPart(required = false)
            MultipartFile profileImage) throws IOException {

        memberService.signUp(signUpRequest, profileImage);
        return ApiResponse.success(signUpRequest.getUserId());
    }

    @PostMapping(value = "/login")
    public ApiResponse<String> login(@RequestBody @Validated LogInRequest logInRequest) {
        String token = memberService.login(logInRequest);
        return ApiResponse.success(token);
    }
    
    @GetMapping("/mypage")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<MyPageResponse> getMyPage(CustomUserDetails userDetails) {

        Long memberId = userDetails.getMemberId();
        MyPageResponse memberInfo = memberService.getMyPageById(memberId);
        return ApiResponse.success(memberInfo);
    }

}

