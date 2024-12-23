package flab.Linkedlog.controller;

import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.member.LogInRequest;
import flab.Linkedlog.dto.member.SignUpRequest;
import flab.Linkedlog.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
                    @ApiResponse(responseCode = "200", description = "회원가입 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청")
            }
    )
    public ResponseEntity<String> createMember(
            @ModelAttribute @Valid SignUpForm signUpForm) throws IOException {
        memberService.signUp(signUpForm.toDto(), signUpForm.getProfileImage());
        return ResponseEntity.ok("회원가입 성공");
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

    @GetMapping("/mypage")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Optional<MyPageDto>> getMyPage() {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Long memberId = userDetails.getMemberId();
        Optional<MyPageDto> memberInfo = Optional.ofNullable(memberService.getMyPageById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("MyPage not found")));
        return ResponseEntity.ok(memberInfo);
    }


}
