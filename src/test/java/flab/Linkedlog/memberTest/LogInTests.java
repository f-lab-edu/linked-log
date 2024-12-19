package flab.Linkedlog.memberTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import flab.Linkedlog.dto.member.LogInRequest;
import flab.Linkedlog.entity.Member;
import flab.Linkedlog.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
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

        // When
        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        // Then
        String responseContent = result.getResponse().getContentAsString();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(responseContent).isNotBlank();

    }

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

        // When
        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        // Then
        String responseContent = result.getResponse().getContentAsString();
        assertThat(result.getResponse().getStatus()).isEqualTo(401);
        assertThat(responseContent).isNotBlank();
    }


    @Test
    @DisplayName("로그인 실패 테스트 - 비밀번호 불일치")
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

        // When
        MvcResult result = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        // Then
        String responseContent = result.getResponse().getContentAsString();
        assertThat(result.getResponse().getStatus()).isEqualTo(401);
        assertThat(responseContent).isNotBlank();
    }


}
