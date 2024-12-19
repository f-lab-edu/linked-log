package flab.Linkedlog;

import flab.Linkedlog.config.JwtProperties;
import flab.Linkedlog.dto.member.LogInRequest;
import flab.Linkedlog.dto.member.SignUpRequest;
import flab.Linkedlog.entity.Member;
import flab.Linkedlog.entity.enums.MemberGrade;
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

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private MemberService memberService;

    public MemberServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("회원가입 테스트")
    void signUpTest() {
        // Given
        SignUpRequest signUpDto = new SignUpRequest("userIdEx", "passwordRaw",
                "nicknameEx", "email", "email.com",
                "010", "1111", "2222");
        Member member = Member.builder()
                .userId("userIdEx")
                .password("passwordEncoded")
                .nickName("nicknameEx")
                .email("email@email.com")
                .phone("010-1111-2222")
                .build();

        // When
        when(passwordEncoder.encode("passwordRaw")).thenReturn("passwordEncoded");
        Long memberId = memberService.signUp(signUpDto);

        // Then
        assertThat(memberId).isEqualTo(member.getId());

    }

    @Test
    void signUpExistTest() {
        // Given
        SignUpRequest signUpDto = new SignUpRequest("userIdEx", "passwordRaw",
                "nicknameEx", "email", "email.com",
                "010", "1111", "2222");
        Member member = Member.builder()
                .userId("userIdEx")
                .password("passwordEncoded")
                .nickName("nicknameEx")
                .email("email@email.com")
                .phone("010-1111-2222")
                .build();

        //When
        when(memberRepository.findByUserId("userIdEx")).thenReturn(Optional.of(member));

        //Then
        assertThrows(IllegalStateException.class, () -> memberService.signUp(signUpDto));
    }

    @Test
    void loginTest() {
        // Given
        LogInRequest logInRequest = new LogInRequest("userIdEx", "passwordRaw");
        Member member = Member.builder()
                .userId("userIdEx")
                .password("passwordEncoded")
                .nickName("nicknameEx")
                .email("email@email.com")
                .phone("010-1111-2222")
                .build();


        // When
        when(memberRepository.findByUserId("userIdEx")).thenReturn(Optional.of(member));
        when(jwtUtil.generateToken(eq("userIdEx"), any(MemberGrade.class))).thenReturn("jwtTokenString");
        when(passwordEncoder.matches("passwordRaw", "passwordEncoded")).thenReturn(true);
        when(jwtProperties.getExpirationTime()).thenReturn(86400000L); // 1일 (밀리초 단위)

        String token = memberService.login(logInRequest);

        // Then
        assertThat(token).isEqualTo("jwtTokenString");
    }

    @Test
    void userNotFoundTest() {
        // Given
        LogInRequest logInRequest = new LogInRequest("userIdEx", "passwordRaw");

        //When
        when(memberRepository.findByUserId("userIdEx")).thenReturn(Optional.empty());

        //Then
        assertThrows(RuntimeException.class, () -> memberService.login(logInRequest));
    }

    @Test
    void invalidPasswordTest() {
        // Given
        LogInRequest logInRequest = new LogInRequest("userIdEx", "passwordRaw");
        Member member = Member.builder()
                .userId("userIdEx")
                .password("passwordEncoded")
                .nickName("nicknameEx")
                .email("email@email.com")
                .phone("010-1111-2222")
                .build();

        //When
        when(memberRepository.findByUserId("userIdEx")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("passwordRaw", "passwordEncoded")).thenReturn(false);

        //Then
        assertThrows(RuntimeException.class, () -> memberService.login(logInRequest));
    }
}
