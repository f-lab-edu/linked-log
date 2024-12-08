package flab.Linkedlog.controller;

import flab.Linkedlog.config.CustomUserDetails;
import flab.Linkedlog.dto.member.LogInDto;
import flab.Linkedlog.dto.member.MyPageDto;
import flab.Linkedlog.dto.member.SignUpDto;
import flab.Linkedlog.dto.member.SignUpForm;
import flab.Linkedlog.dto.post.PostDetailDto;
import flab.Linkedlog.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    }


    @PostMapping(value = "/login")
    public ResponseEntity<String> login(@RequestBody @Validated LogInDto loginDto) {

        String token = memberService.login(loginDto);
        return ResponseEntity.ok(token);

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
