package flab.Linkedlog.controller;

import flab.Linkedlog.config.CustomUserDetails;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.MainResponse;
import flab.Linkedlog.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MainController {

    private final MemberService memberService;

    @GetMapping(value = "/main")
    public ApiResponse<MainResponse> getMainInfo(
            @AuthenticationPrincipal Object principal) {

        if (principal == null || principal instanceof String) {
            MainResponse mainResponseForGuest = memberService.getMainForGuest();
            return ApiResponse.success(mainResponseForGuest);
        }

        CustomUserDetails userDetails = (CustomUserDetails) principal;
        Long memberId = userDetails.getMemberId();
        MainResponse mainResponse = memberService.getMain(memberId);

        return ApiResponse.success(mainResponse);
    }
}
