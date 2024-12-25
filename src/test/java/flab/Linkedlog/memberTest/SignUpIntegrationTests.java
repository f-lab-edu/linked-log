package flab.Linkedlog.memberTest;


import com.fasterxml.jackson.databind.ObjectMapper;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.member.SignUpForm;
import flab.Linkedlog.dto.member.SignUpRequest;
import flab.Linkedlog.entity.Member;
import flab.Linkedlog.entity.enums.MemberGrade;
import flab.Linkedlog.entity.enums.MemberStatus;
import flab.Linkedlog.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

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
        SignUpForm signUpForm = SignUpForm.builder()
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
        MvcResult result = mockMvc.perform(multipart("/signup")
                        .param("userId", signUpForm.getUserId())        // 폼 데이터 전송
                        .param("password", signUpForm.getPassword())
                        .param("nickname", signUpForm.getNickname())
                        .param("email1", signUpForm.getEmail1())
                        .param("email2", signUpForm.getEmail2())
                        .param("phone1", signUpForm.getPhone1())
                        .param("phone2", signUpForm.getPhone2())
                        .param("phone3", signUpForm.getPhone3())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andReturn();


        // Then
        String jsonResponse = result.getResponse().getContentAsString();
        ApiResponse response = objectMapper.readValue(jsonResponse, ApiResponse.class);


        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(response.getResponse()).isNotNull();

        Member member = memberRepository.findByUserId("testUser")
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다"));

        assertThat(member).isNotNull();
        assertThat(member.getUserId()).isEqualTo("testUser");
        assertThat(member.getNickName()).isEqualTo("testNickname");

        // parsing test
        assertThat(member.getEmail()).isEqualTo("emailfront@gmail.com");
        assertThat(member.getPhone()).isEqualTo("010-1111-2345");

        // default column test
        assertThat(member.getCashPoint()).isEqualTo(BigDecimal.ZERO);
        assertThat(member.getDeletedAt()).isNull();
        assertThat(member.getMemberStatus()).isEqualTo(MemberStatus.NORMAL);
        assertThat(member.getMemberGrade()).isEqualTo(MemberGrade.GENERAL);

        // password Encoding Test
        assertThat(passwordEncoder.matches("testpasswd", member.getPassword())).isTrue();
    }


    @Test
    @DisplayName("회원 가입 중복 테스트")
    void signUpDuplicateTest() throws Exception {

        SignUpForm initialSignUpForm = SignUpForm.builder()
                .userId("member01")
                .password("passwd1")
                .nickname("멤버1")
                .email1("member01")
                .email2("gmail.com")
                .phone1("010")
                .phone2("1111")
                .phone3("3333")
                .build();

        MvcResult initialResult = mockMvc.perform(multipart("/signup")
                        .param("userId", initialSignUpForm.getUserId())
                        .param("password", initialSignUpForm.getPassword())
                        .param("nickname", initialSignUpForm.getNickname())
                        .param("email1", initialSignUpForm.getEmail1())
                        .param("email2", initialSignUpForm.getEmail2())
                        .param("phone1", initialSignUpForm.getPhone1())
                        .param("phone2", initialSignUpForm.getPhone2())
                        .param("phone3", initialSignUpForm.getPhone3())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andReturn();

        String initialResponseContent = initialResult.getResponse().getContentAsString();
        ApiResponse initialResponse = objectMapper.readValue(initialResponseContent, ApiResponse.class);

        assertThat(initialResult.getResponse().getStatus()).isEqualTo(200);
        assertThat(initialResponse.getResponse()).isEqualTo("member01");

        SignUpForm duplicateSignUpForm = SignUpForm.builder()
                .userId("member01")
                .password("passwd2")
                .nickname("멤버2")
                .email1("member02")
                .email2("gmail.com")
                .phone1("010")
                .phone2("2222")
                .phone3("3333")
                .build();

        MvcResult duplicateResult = mockMvc.perform(multipart("/signup")
                        .param("userId", duplicateSignUpForm.getUserId())
                        .param("password", duplicateSignUpForm.getPassword())
                        .param("nickname", duplicateSignUpForm.getNickname())
                        .param("email1", duplicateSignUpForm.getEmail1())
                        .param("email2", duplicateSignUpForm.getEmail2())
                        .param("phone1", duplicateSignUpForm.getPhone1())
                        .param("phone2", duplicateSignUpForm.getPhone2())
                        .param("phone3", duplicateSignUpForm.getPhone3())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andReturn();

        String duplicateResponseContent = duplicateResult.getResponse().getContentAsString();
        ApiResponse duplicateResponse = objectMapper.readValue(duplicateResponseContent, ApiResponse.class);

        assertThat(duplicateResult.getResponse().getStatus()).isEqualTo(500);
        assertThat(duplicateResponse.getError()).isEqualTo("INVALID_STATE");

        // Verify that the initial member still exists in the repository
        Member existingMember = memberRepository.findByUserId("member01")
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다"));
        assertThat(existingMember).isNotNull();
        assertThat(existingMember.getUserId()).isEqualTo("member01");
    }

    @Test
    @DisplayName("프로필 이미지 등록 테스트")
    void signUpWithProfileImageTest() throws Exception {

        // Given
        SignUpForm signUpForm = SignUpForm.builder()
                .userId("testUser")
                .password("testpasswd")
                .nickname("testNickname")
                .email1("emailfront")
                .email2("gmail.com")
                .phone1("010")
                .phone2("1111")
                .phone3("2345")
                .build();

        MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage",
                "test-image.png",
                MediaType.IMAGE_PNG_VALUE,
                "dummy image content".getBytes(StandardCharsets.UTF_8));

        // When
        MvcResult result = mockMvc.perform(multipart("/signup")
                        .file("profileImage", profileImage.getBytes())  // 파일 전송
                        .param("userId", signUpForm.getUserId())        // 폼 데이터 전송
                        .param("password", signUpForm.getPassword())
                        .param("nickname", signUpForm.getNickname())
                        .param("email1", signUpForm.getEmail1())
                        .param("email2", signUpForm.getEmail2())
                        .param("phone1", signUpForm.getPhone1())
                        .param("phone2", signUpForm.getPhone2())
                        .param("phone3", signUpForm.getPhone3())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andReturn();


        // Then
        String jsonResponse = result.getResponse().getContentAsString();
        ApiResponse response = objectMapper.readValue(jsonResponse, ApiResponse.class);


        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(response.getResponse()).isNotNull();


        Member member = memberRepository.findByUserId("testUser").orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다"));

        assertThat(member).isNotNull();
        assertThat(member.getProfileImage()).isNotNull();

//        String profileImageFileName = member.getProfileImage();
//        assertThat(profileImageFileName).endsWith("test-image.png");
//        assertThat(profileImageFileName).contains("_");

    }


}