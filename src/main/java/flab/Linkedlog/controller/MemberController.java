package flab.Linkedlog.controller;

import flab.Linkedlog.dto.member.LogInDto;
import flab.Linkedlog.dto.member.SignUpDto;
import flab.Linkedlog.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping(value = "/signup")
    public ResponseEntity<Boolean> createMember(@RequestBody @Validated SignUpDto signUpDto) {
        memberService.signUp(signUpDto);
        return ResponseEntity.ok(true);
    }


    @PostMapping(value = "/login")
    public ResponseEntity<String> login(@RequestBody @Validated LogInDto loginDto) {

        String token = memberService.login(loginDto);
        return ResponseEntity.ok(token);

    }


}
