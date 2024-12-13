package flab.Linkedlog.memberTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import flab.Linkedlog.dto.member.LogInRequest;
import flab.Linkedlog.entity.Member;
import flab.Linkedlog.repository.MemberRepository;
import flab.Linkedlog.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class LogInTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;


    // 로그인 성공 테스트
    @Test
    @DisplayName("로그인 성공 테스트")
    void loginSuccessfulTest() throws Exception {

        String userId = "testUser";
        String password = "testpasswd";

        Member member = Member.builder()
                .userId(userId)
                .password(passwordEncoder.encode(password))
                .nickName("testNickname")
                .email("testUser@gmail.com")
                .phone("010-1111-2222")
                .build();
        memberRepository.save(member);

        LogInRequest loginRequest = LogInRequest.builder()
                .userId(userId)
                .password(password)
                .build();

        // When & Then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.response.data").exists());
    }

    // 로그인 실패 (아이디 불일치)
    @Test
    @DisplayName("로그인 실패 테스트 - 잘못된 아이디")
    void loginFailureTestInvalidUserId() throws Exception {

        // Given
        String userId = "testUser";
        String password = "testpasswd";

        Member member = Member.builder()
                .userId(userId)
                .password(passwordEncoder.encode(password))
                .nickName("testNickname")
                .email("testUser@gmail.com")
                .phone("010-1111-2222")
                .build();
        memberRepository.save(member);


        String wrongUserId = "invalidUser";
        String newPassword = "testpasswd";

        LogInRequest loginRequest = LogInRequest.builder()
                .userId(wrongUserId)
                .password(newPassword)
                .build();

        // When & Then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
    }

    // 로그인 실패 (비밀번호 불일치)
    @Test
    @DisplayName("로그인 실패 테스트 - 잘못된 비밀번호")
    void loginFailureTestInvalidPassword() throws Exception {

        // Given
        String userId = "testUser";
        String password = "testpasswd";

        Member member = Member.builder()
                .userId(userId)
                .password(passwordEncoder.encode(password))
                .nickName("testNickname")
                .email("testUser@gmail.com")
                .phone("010-1111-2222")
                .build();
        memberRepository.save(member);

        String wrongPassword = "wrongPassword";

        LogInRequest loginRequest = LogInRequest.builder()
                .userId(userId)
                .password(wrongPassword)
                .build();

        // When & Then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
    }


}
