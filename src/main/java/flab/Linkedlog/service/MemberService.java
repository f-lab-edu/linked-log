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

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 회원가입
    public UUID signUp(SignUpDto signUpDto) {

        String userId = signUpDto.getUserId();
        String rawPassword = signUpDto.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword); // 비밀번호 암호화
        String nickname = signUpDto.getNickname();
        String email = signUpDto.getEmail1() + "@" + signUpDto.getEmail2();
        String phone = signUpDto.getPhone1() + "-" +
                signUpDto.getPhone2() + "-" + signUpDto.getPhone3();

        Member member = new Member(userId, encodedPassword, nickname, email, phone);

        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();

    }

    public void validateDuplicateMember(Member member) {
        Member findMember = memberRepository.findByUserId(member.getUserId());
        if (findMember != null) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    // 로그인
    public String login(LogInDto logInDto) {

        String userId = logInDto.getUserId();
        String password = logInDto.getPassword();
        Member member = memberRepository.findByUserId(userId);

        if (member == null) {
            throw new RuntimeException("User not found");
        }

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtUtil.generateToken(member.getUserId(), member.getMemberGrade());
    }



}



