package flab.Linkedlog.chatTest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.entity.ChatMember;
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


import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class DeleteChatRoomTest {

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
    @DisplayName("채팅방 삭제 성공 테스트")
    void deleteChatRoomSuccessTest() throws Exception {
        // Given
        ChatRoom chatRoom = ChatRoom.builder()
                .title("Test Chat Room")
                .maxParticipants(5)
                .chatRoomType(ChatRoomType.GROUP)
                .member(memberRepository.findById(testMemberId).orElseThrow())
                .password(null)
                .build();
        chatRoom = chatRoomRepository.save(chatRoom);

        ChatMember chatMember = ChatMember.builder()
                .chatRoom(chatRoom)
                .member(memberRepository.findById(testMemberId).orElseThrow())
                .build();
        chatMemberRepository.save(chatMember);

        Long chatRoomId = chatRoom.getId();

        // When
        MvcResult result = mockMvc.perform(post("/chat/chatroom/" + chatRoomId + "/delete")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = result.getResponse().getContentAsString();
        ApiResponse<Long> response = objectMapper.readValue(jsonResponse, new TypeReference<ApiResponse<Long>>() {
        });

        assertThat(response.getResponse()).isNotNull();
        assertThat(response.getResponse()).isEqualTo(chatRoomId);

        Optional<ChatRoom> deletedChatRoom = chatRoomRepository.findById(chatRoomId);
        assertThat(deletedChatRoom).isPresent();
        assertThat(deletedChatRoom.get().getDeletedAt()).isNotNull();

        List<ChatMember> chatMembers = chatMemberRepository.findByChatRoom(chatRoom);
        assertThat(chatMembers.get(0).getDeletedAt()).isNotNull();
    }
}
