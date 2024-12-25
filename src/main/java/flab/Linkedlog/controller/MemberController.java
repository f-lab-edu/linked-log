package flab.Linkedlog.controller;

import flab.Linkedlog.config.CustomUserDetails;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.member.LogInRequest;
import flab.Linkedlog.dto.member.MyPageResponse;
import flab.Linkedlog.dto.member.SignUpForm;
import flab.Linkedlog.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping(value = "/signup", consumes = "multipart/form-data")
    @Operation(
            summary = "회원가입",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
                    mediaType = "multipart/form-data",
                    schema = @Schema(implementation = SignUpForm.class)
            )),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 성공"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
            }
    )
    public ApiResponse<String> createMember(
            @ModelAttribute @Valid SignUpForm signUpForm) throws IOException {
        memberService.signUp(signUpForm.toDto(), signUpForm.getProfileImage());
        return ApiResponse.success(signUpForm.getUserId());
    }

//    @PostMapping(value = "/signup")
//    public ApiResponse<String> createMember(@RequestBody @Validated SignUpRequest signUpRequest) {
//        memberService.signUp(SignUpRequest.builder().build(), signUpForm.getProfileImage());
//
//        return ApiResponse.success(signUpRequest.getUserId());
//
//    }


    @PostMapping(value = "/login")
    public ApiResponse<String> login(@RequestBody @Validated LogInRequest loginRequest) {
        String token = memberService.login(loginRequest);

        return ApiResponse.success(token);

    }

    @GetMapping("/mypage")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Optional<MyPageResponse>> getMyPage() {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Long memberId = userDetails.getMemberId();
        Optional<MyPageResponse> memberInfo = Optional.ofNullable(memberService.getMyPageById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("MyPage not found")));
        return ApiResponse.success(memberInfo);
    }

}

