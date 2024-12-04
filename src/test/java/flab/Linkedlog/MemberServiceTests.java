package flab.Linkedlog;

import flab.Linkedlog.dto.member.LogInDto;
import flab.Linkedlog.dto.member.SignUpDto;
import flab.Linkedlog.entity.Member;
import flab.Linkedlog.repository.MemberRepository;
import flab.Linkedlog.service.MemberService;
import flab.Linkedlog.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Transactional
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private MemberService memberService;

    public MemberServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("회원가입 테스트")
    void signUpTest() {
        // Given
        SignUpDto signUpDto = new SignUpDto("userIdEx", "passwordRaw",
                "nicknameEx", "email", "email.com",
                "010", "1111", "2222");
        Member member = new Member("userIdEx", "passwordEncoded",
                "nicknameEx", "email@email.com", "010-1111-2222");

        // When
        when(passwordEncoder.encode("passwordRaw")).thenReturn("passwordEncoded");
        Long memberId = memberService.signUp(signUpDto);

        // Then
        assertThat(memberId).isEqualTo(member.getId());

    }

    @Test
    void signUpExistTest() {
        // Given
        SignUpDto signUpDto = new SignUpDto("userIdEx", "passwordRaw",
                "nicknameEx", "email", "email.com",
                "010", "1111", "2222");
        Member member = new Member("userIdEx", "passwordEncoded",
                "nicknameEx", "email@email.com", "010-1111-2222");

        //When
        when(memberRepository.findByUserId("userIdEx")).thenReturn(Optional.of(member));

        //Then
        assertThrows(IllegalStateException.class, () -> memberService.signUp(signUpDto));
    }

    @Test
    void loginTest() {
        // Given
        LogInDto logInDto = new LogInDto("userIdEx", "passwordRaw");
        Member member = new Member("userIdEx", "passwordEncoded",
                "nicknameEx", "email@email.com", "010-1111-2222");

        // When
        when(memberRepository.findByUserId("userIdEx")).thenReturn(Optional.of(member));
        when(jwtUtil.generateToken("userIdEx")).thenReturn("jwtTokenString");
        when(passwordEncoder.matches("passwordRaw", "passwordEncoded")).thenReturn(true);

        String token = memberService.login(logInDto);

        // Then
        assertThat(token).isEqualTo("jwtTokenString");
    }

    @Test
    void userNotFoundTest() {
        // Given
        LogInDto logInDto = new LogInDto("userIdEx", "passwordRaw");

        //When
        when(memberRepository.findByUserId("userIdEx")).thenReturn(Optional.empty());

        //Then
        assertThrows(RuntimeException.class, () -> memberService.login(logInDto));
    }

    @Test
    void invalidPasswordTest() {
        // Given
        LogInDto logInDto = new LogInDto("userIdEx", "passwordRaw");
        Member member = new Member("userIdEx", "passwordEncoded",
                "nicknameEx", "email@email.com", "010-1111-2222");

        //When
        when(memberRepository.findByUserId("userIdEx")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("passwordRaw", "passwordEncoded")).thenReturn(false);

        //Then
        assertThrows(RuntimeException.class, () -> memberService.login(logInDto));
    }
}
