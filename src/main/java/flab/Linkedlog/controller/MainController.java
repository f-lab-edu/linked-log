package flab.Linkedlog.controller;


import flab.Linkedlog.config.CustomUserDetails;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.MainResponse;
import flab.Linkedlog.repository.MemberRepository;
import flab.Linkedlog.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MainController {

    private final MemberService memberService;
    MemberRepository memberRepository;

    @GetMapping(value = "/main")
    public ApiResponse<MainResponse> getMainInfo(
            CustomUserDetails userDetails) {

        if (userDetails == null) {
            MainResponse mainResponseForGuest = memberService.getMainForGuest(); // 게스트용 응답
            return ApiResponse.success(mainResponseForGuest);
        }

        Long id = userDetails.getMemberId();
        MainResponse mainResponse = memberService.getMain(id);

        return ApiResponse.success(mainResponse);
    }

}

