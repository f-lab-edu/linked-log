package flab.Linkedlog.controller;

import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.member.LogInRequest;
import flab.Linkedlog.dto.member.SignUpRequest;
import flab.Linkedlog.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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


//    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ApiResponse<String> createMember(
//            @Valid @RequestPart SignUpRequest signUpRequest,
//            @RequestPart(required = false) MultipartFile profileImage) throws IOException {
//        memberService.signUp(signUpRequest, profileImage);
//        return ApiResponse.success(signUpRequest.getUserId());
//    }

    @PostMapping(value = "/login")
    public ApiResponse<String> login(@RequestBody @Validated LogInRequest logInRequest) {
        String token = memberService.login(logInRequest);
        return ApiResponse.success(token);
    }

//
//    @GetMapping("/mypage")
//    @PreAuthorize("isAuthenticated()")
//    public ApiResponse<Optional<MyPageResponse>> getMyPage() {
//
//        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//
//        Long memberId = userDetails.getMemberId();
//        Optional<MyPageResponse> memberInfo = Optional.ofNullable(memberService.getMyPageById(memberId)
//                .orElseThrow(() -> new EntityNotFoundException("MyPage not found")));
//        return ApiResponse.success(memberInfo);
//    }

}

