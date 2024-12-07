package flab.Linkedlog.service;

import flab.Linkedlog.dto.member.LogInDto;
import flab.Linkedlog.dto.member.SignUpDto;
import flab.Linkedlog.entity.Member;
import flab.Linkedlog.repository.MemberRepository;
import flab.Linkedlog.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 회원가입
    public Long signUp(SignUpDto signUpDto) {

        String userId = signUpDto.getUserId();
        String rawPassword = signUpDto.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword); // 비밀번호 암호화
        String nickname = signUpDto.getNickname();
        String email = signUpDto.getEmail1() + "@" + signUpDto.getEmail2();
        String phone = signUpDto.getPhone1() + "-" +
                signUpDto.getPhone2() + "-" + signUpDto.getPhone3();

        Member member = Member.builder()
                .userId(userId)
                .password(encodedPassword)
                .nickName(nickname)
                .email(email)
                .phone(phone)
                .build();

        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();

    }

    public void validateDuplicateMember(Member member) {
        Optional<Member> findMember = memberRepository.findByUserId(member.getUserId());
        if (findMember.isPresent()) {
            throw new IllegalStateException("User Already Exists.");
        }
    }

    // 로그인
    public String login(LogInDto logInDto) {

        String userId = logInDto.getUserId();
        String password = logInDto.getPassword();
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        if (member == null) {
            throw new RuntimeException("User not found");
        }

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtUtil.generateToken(member.getUserId());
    }


}



