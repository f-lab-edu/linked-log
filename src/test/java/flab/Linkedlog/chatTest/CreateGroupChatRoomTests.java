package flab.Linkedlog.chatTest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.chat.ChatRoomCreateRequest;
import flab.Linkedlog.entity.ChatRoom;
import flab.Linkedlog.entity.Member;
import flab.Linkedlog.entity.enums.ChatRoomType;
import flab.Linkedlog.entity.enums.MemberGrade;
import flab.Linkedlog.repository.MemberRepository;
import flab.Linkedlog.repository.chat.ChatMemberRepository;
import flab.Linkedlog.repository.chat.ChatRoomRepository;
import flab.Linkedlog.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class CreateGroupChatRoomTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMemberRepository chatMemberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long testMemberId;
    @Value("${profile.default-image-url}")
    private String defaultProfileImage;
    String token;

    @BeforeEach
    void setUp() {
        Member testMember = Member.builder()
                .userId("testUser")
                .password("testPassword")
                .nickName("testNickName")
                .email("testemail@test.com")
                .phone("010-0000-0000")
                .memberGrade(MemberGrade.GENERAL)
                .profileImage(defaultProfileImage)
                .build();
        testMember = memberRepository.save(testMember);
        testMemberId = testMember.getId();

        token = jwtUtil.generateToken("testUser", MemberGrade.GENERAL, testMemberId);
    }

    @AfterEach
    void tearDown() {
        chatMemberRepository.deleteAll();
        chatRoomRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("그룹 채팅방 생성 성공 테스트")
    void createGroupChatRoomSuccessTest() throws Exception {
        // Given
        ChatRoomCreateRequest chatRoomCreateRequest = ChatRoomCreateRequest.builder()
                .title("Test Group Chat")
                .password(null)
                .maxParticipants(5)
                .build();
        String chatRoomCreateRequestJson = objectMapper.writeValueAsString(chatRoomCreateRequest);

        // When
        MvcResult result = mockMvc.perform(post("/chat/create/group")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(chatRoomCreateRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = result.getResponse().getContentAsString();
        ApiResponse<Long> response = objectMapper.readValue(jsonResponse, new TypeReference<ApiResponse<Long>>() {
        });

        System.out.println("Response memberId Type: " + ((Object) response.getResponse()).getClass().getName()); // response.getResponse()의 클래스 이름 출력

        assertThat(response.getResponse()).isNotNull();

        Optional<ChatRoom> savedChatRoom = chatRoomRepository.findAll().stream()
                .filter(chatRoom -> "Test Group Chat".equals(chatRoom.getTitle()))
                .findFirst();

        assertThat(savedChatRoom).isPresent();
        assertThat(savedChatRoom.get().getTitle()).isEqualTo("Test Group Chat");
        assertThat(savedChatRoom.get().getMaxParticipants()).isEqualTo(5);
        assertThat(savedChatRoom.get().getChatRoomType()).isEqualTo(ChatRoomType.GROUP);
        assertThat(savedChatRoom.get().getPassword()).isNull();
    }


    @Test
    @DisplayName("그룹 채팅방 암호 포함 성공 테스트")
    void createGroupChatRoomPasswordSuccessTest() throws Exception {
        // Given
        ChatRoomCreateRequest chatRoomCreateRequest = ChatRoomCreateRequest.builder()
                .title("Test Group Chat")
                .password("testPassword")
                .maxParticipants(5)
                .build();
        String chatRoomCreateRequestJson = objectMapper.writeValueAsString(chatRoomCreateRequest);

        // When
        MvcResult result = mockMvc.perform(post("/chat/create/group")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(chatRoomCreateRequestJson))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = result.getResponse().getContentAsString();
        ApiResponse response = objectMapper.readValue(jsonResponse, ApiResponse.class);

        assertThat(response.getResponse()).isNotNull();

        Optional<ChatRoom> savedChatRoom = chatRoomRepository.findAll().stream()
                .filter(chatRoom -> "Test Group Chat".equals(chatRoom.getTitle()))
                .findFirst();

        assertThat(savedChatRoom).isPresent();
        assertThat(savedChatRoom.get().getTitle()).isEqualTo("Test Group Chat");
        assertThat(savedChatRoom.get().getMaxParticipants()).isEqualTo(5);
        assertThat(savedChatRoom.get().getChatRoomType()).isEqualTo(ChatRoomType.GROUP);
        assertThat(passwordEncoder.matches("testPassword", savedChatRoom.get().getPassword())).isTrue();
    }

    @Test
    @DisplayName("그룹 채팅방 생성 인원수 실패 테스트")
    void createGroupChatRoomMaxParticipantsFailTest() throws Exception {
        // Given
        ChatRoomCreateRequest chatRoomCreateRequest = ChatRoomCreateRequest.builder()
                .title("Test Group Chat")
                .password(null)
                .maxParticipants(1)
                .build();
        String chatRoomCreateRequestJson = objectMapper.writeValueAsString(chatRoomCreateRequest);

        // When & Then
        MvcResult result = mockMvc.perform(post("/chat/create/group")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(chatRoomCreateRequestJson))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    @DisplayName("그룹 채팅방 생성 이름 누락 테스트")
    void createGroupChatRoomTitleFailTest() throws Exception {
        // Given
        ChatRoomCreateRequest chatRoomCreateRequest = ChatRoomCreateRequest.builder()
                .title(null)
                .password(null)
                .maxParticipants(5)
                .build();
        String chatRoomCreateRequestJson = objectMapper.writeValueAsString(chatRoomCreateRequest);

        // When & Then
        MvcResult result = mockMvc.perform(post("/chat/create/group")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(chatRoomCreateRequestJson))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    @DisplayName("그룹 채팅방 생성 비회원 접근 권한 테스트")
    void createGroupChatRoomLogoutFailTest() throws Exception {
        // Given
        ChatRoomCreateRequest chatRoomCreateRequest = ChatRoomCreateRequest.builder()
                .title("Test Group Chat")
                .password(null)
                .maxParticipants(5)
                .build();
        String chatRoomCreateRequestJson = objectMapper.writeValueAsString(chatRoomCreateRequest);

        // When & Then
        MvcResult result = mockMvc.perform(post("/chat/create/group")
                        .contentType("application/json")
                        .content(chatRoomCreateRequestJson))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }
}
