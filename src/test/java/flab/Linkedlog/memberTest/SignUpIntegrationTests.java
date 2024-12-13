package flab.Linkedlog.memberTest;


import com.fasterxml.jackson.databind.ObjectMapper;
import flab.Linkedlog.dto.member.SignUpRequest;
import flab.Linkedlog.entity.Member;
import flab.Linkedlog.entity.enums.MemberGrade;
import flab.Linkedlog.entity.enums.MemberStatus;
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

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail; // fail 메소드를 static import
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class SignUpIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Test
    @DisplayName("회원 가입 성공 테스트")
    void signUpSuccessfulTest() throws Exception {

        // Given
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .userId("testUser")
                .password("testpasswd")
                .nickname("testNickname")
                .email1("emailfront")
                .email2("gmail.com")
                .phone1("010")
                .phone2("1111")
                .phone3("2345")
                .build();

        // When
        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.response.data").value("testUser"));

        Member member = memberRepository.findByUserId("testUser").orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다"));

        // Then
        assertNotNull(member);
        assertEquals("testUser", member.getUserId());
        assertEquals("testNickname", member.getNickName());

        // parsing test
        assertEquals("emailfront@gmail.com", member.getEmail());
        assertEquals("010-1111-2345", member.getPhone());

        // default column test
        assertEquals(BigDecimal.ZERO, member.getCashPoint());
        assertEquals(null, member.getDeletedAt());
        assertEquals(MemberStatus.NORMAL, member.getMemberStatus());
        assertEquals(MemberGrade.GENERAL, member.getMemberGrade());

        // password Encoding Test
        assertTrue(passwordEncoder.matches("testpasswd", member.getPassword()));


    }

    @Test
    @DisplayName("회원 가입 중복 테스트")
    void signUpDuplicateTest() throws Exception {

        // Given
        SignUpRequest initialSignUpRequest = SignUpRequest.builder()
                .userId("member01")
                .password("passwd1")
                .nickname("멤버1")
                .email1("member01")
                .email2("gmail.com")
                .phone1("010")
                .phone2("1111")
                .phone3("3333")
                .build();

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initialSignUpRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk()) // 응답 상태가 200 OK
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.response.data").value("member01"));

        SignUpRequest duplicateSignUpRequest = SignUpRequest.builder()
                .userId("member01")
                .password("passwd2")
                .nickname("멤버2")
                .email1("member02")
                .email2("gmail.com")
                .phone1("010")
                .phone2("2222")
                .phone3("3333")
                .build();

        // When
        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateSignUpRequest)))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));

        Member existingMember = memberRepository.findByUserId("member01").orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다"));

        // Then
        assertNotNull(existingMember);
        assertEquals("member01", existingMember.getUserId());


    }


}