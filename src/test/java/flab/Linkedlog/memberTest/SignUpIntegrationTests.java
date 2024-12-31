package flab.Linkedlog.memberTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.member.SignUpRequest;
import flab.Linkedlog.entity.Member;
import flab.Linkedlog.entity.enums.MemberGrade;
import flab.Linkedlog.entity.enums.MemberStatus;
import flab.Linkedlog.repository.MemberRepository;
import flab.Linkedlog.service.S3Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Autowired
    private S3Service s3Service;

    @Value("${aws.s3.bucket}")
    private String bucketName;


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

        // Convert SignUpRequest to JSON
        String signUpRequestJson = new ObjectMapper().writeValueAsString(signUpRequest);

        // Create a MockMultipartFile for signUpRequest
        MockMultipartFile signUpRequestPart = new MockMultipartFile(
                "signUpRequest",
                "signUpRequest.json",
                "application/json",
                signUpRequestJson.getBytes()
        );

        // When
        MvcResult result = mockMvc.perform(multipart("/signup")
                        .file("profileImage", new byte[0])
                        .file(signUpRequestPart)
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

        // Convert SignUpRequest to JSON
        String signUpRequestJson = new ObjectMapper().writeValueAsString(initialSignUpRequest);

        // Create a MockMultipartFile for signUpRequest
        MockMultipartFile signUpRequestPart = new MockMultipartFile(
                "signUpRequest",
                "signUpRequest.json",
                "application/json",
                signUpRequestJson.getBytes()
        );

        // When (initial sign up)
        MvcResult initialResult = mockMvc.perform(multipart("/signup")
                        .file("profileImage", new byte[0])
                        .file(signUpRequestPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        String initialResponseContent = initialResult.getResponse().getContentAsString();
        ApiResponse initialResponse = objectMapper.readValue(initialResponseContent, ApiResponse.class);

        assertThat(initialResponse.getResponse()).isEqualTo("member01");

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

        String duplicateSignUpRequestJson = new ObjectMapper().writeValueAsString(duplicateSignUpRequest);

        MockMultipartFile duplicateSignUpRequestPart = new MockMultipartFile(
                "signUpRequest",
                "signUpRequest.json",
                "application/json",
                duplicateSignUpRequestJson.getBytes()
        );

        // When
        MvcResult duplicateResult = mockMvc.perform(multipart("/signup")
                        .file("profileImage", new byte[0])
                        .file(duplicateSignUpRequestPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError())
                .andReturn();

        String duplicateResponseContent = duplicateResult.getResponse().getContentAsString();
        ApiResponse duplicateResponse = objectMapper.readValue(duplicateResponseContent, ApiResponse.class);

        assertThat(duplicateResponse.getError()).isEqualTo("INVALID_STATE");

        Member existingMember = memberRepository.findByUserId("member01")
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다"));
        assertThat(existingMember).isNotNull();
        assertThat(existingMember.getUserId()).isEqualTo("member01");
    }

    @Test
    @DisplayName("프로필 이미지 등록 테스트")
    void signUpWithProfileImageTest() throws Exception {

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

        String signUpRequestJson = new ObjectMapper().writeValueAsString(signUpRequest);

        MockMultipartFile signUpRequestPart = new MockMultipartFile(
                "signUpRequest",
                "signUpRequest.json",
                "application/json",
                signUpRequestJson.getBytes()
        );

        MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage",
                "test-image.png",
                MediaType.IMAGE_PNG_VALUE,
                "dummy image content".getBytes(StandardCharsets.UTF_8)
        );

        // When
        MvcResult result = mockMvc.perform(multipart("/signup")
                        .file("profileImage", profileImage.getBytes())
                        .file(signUpRequestPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = result.getResponse().getContentAsString();
        ApiResponse response = objectMapper.readValue(jsonResponse, ApiResponse.class);

        assertThat(response.getResponse()).isNotNull();

        Member member = memberRepository.findByUserId("testUser").orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다"));

        assertThat(member).isNotNull();
        assertThat(member.getProfileImage()).isNotNull();
//        String profileImageFileName = member.getProfileImage();
//        assertThat(profileImageFileName).endsWith("test-image.png");
//        assertThat(profileImageFileName).contains("_");

        String profileImageUrl = member.getProfileImage();
        String profileImageKey = profileImageUrl.substring(profileImageUrl.lastIndexOf("/") + 1);

        String s3Url = s3Service.getFileUrl(profileImageKey, bucketName);
        assertThat(s3Url).contains(profileImageKey);

        s3Service.deleteFile(profileImageKey, bucketName);

    }


}