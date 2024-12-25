package flab.Linkedlog.controller;

import flab.Linkedlog.config.CustomUserDetails;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.member.LogInRequest;
import flab.Linkedlog.dto.member.MyPageResponse;
import flab.Linkedlog.dto.member.SignUpForm;
import flab.Linkedlog.dto.member.SignUpRequest;
import flab.Linkedlog.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> createMember(
            @Valid @RequestPart SignUpRequest signUpRequest,
            @RequestPart(required = false) MultipartFile profileImage) throws IOException {
        memberService.signUp(signUpRequest, profileImage);
        return ApiResponse.success(signUpRequest.getUserId());
    }


    /*
@PostMapping(value = "{partnerDomain}/products/{travelProductPartnerCustomId}/reviews"**, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)**
public ResponseEntity<Void> createReview(@PathVariable String partnerDomain,
                                         @PathVariable String travelProductPartnerCustomId,
                                         @Valid @RequestPart ReviewCreateRequest reviewCreateRequest,
                                         @RequestPart(required = false) List<MultipartFile> reviewImageFiles) {

     * */

//    @PostMapping(value = "/signup")
//    public ApiResponse<String> createMember(@RequestBody @Validated SignUpRequest signUpRequest) {
//        memberService.signUp(SignUpRequest.builder().build(), signUpForm.getProfileImage());
//
//        return ApiResponse.success(signUpRequest.getUserId());
//
//    }

    @PostMapping(value = "/login")
    public ApiResponse<String> login(@RequestBody LogInRequest loginRequest) {
        Logger logger = LoggerFactory.getLogger(MemberController.class);
        logger.info("Received loginRequest: {}", loginRequest);
        logger.info("userId: {}", loginRequest.getUserId());
        logger.info("password: {}", loginRequest.getPassword());
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

