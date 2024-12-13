package flab.Linkedlog.controller;

import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.controller.response.ErrorResponse;
import flab.Linkedlog.controller.response.SuccessResponse;
import flab.Linkedlog.dto.member.LogInRequest;
import flab.Linkedlog.dto.member.SignUpRequest;
import flab.Linkedlog.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

    @PostMapping(value = "/signup")
    public ResponseEntity<ApiResponse<?>> createMember(@RequestBody @Validated SignUpRequest signUpRequest) {
        try {
            memberService.signUp(signUpRequest);
            SuccessResponse<String> successResponse = new SuccessResponse<>(signUpRequest.getUserId());
            ApiResponse<SuccessResponse<String>> apiResponse = ApiResponse.<SuccessResponse<String>>builder()
                    .success(true)
                    .response(successResponse)
                    .build();
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            logger.error("회원 가입 중 오류 발생: {}", e.getMessage(), e);

            ErrorResponse errorResponse = new ErrorResponse("SIGNUP_FAILED", "회원 가입 중 오류가 발생했습니다.");
            ApiResponse<ErrorResponse> apiResponse = ApiResponse.<ErrorResponse>builder()
                    .success(false)
                    .response(errorResponse)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }


    @PostMapping(value = "/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody @Validated LogInRequest loginRequest) {
        try {
            String token = memberService.login(loginRequest);
            SuccessResponse<String> successResponse = new SuccessResponse<>(token);
            ApiResponse<SuccessResponse<String>> apiResponse = ApiResponse.<SuccessResponse<String>>builder()
                    .success(true)
                    .response(successResponse)
                    .build();
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse("LOGIN_FAILED", "로그인 중 오류가 발생했습니다.");
            ApiResponse<ErrorResponse> apiResponse = ApiResponse.<ErrorResponse>builder()
                    .success(false)
                    .response(errorResponse)
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResponse);
        }
    }


}
