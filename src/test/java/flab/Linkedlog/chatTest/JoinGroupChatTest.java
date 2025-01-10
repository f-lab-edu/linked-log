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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class JoinGroupChatTest {

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

    private Long creatorId;
    private Long participantId;
    private Long chatRoomId;
    private String participantToken;

    @Value("${profile.default-image-url}")
    private String defaultProfileImage;

    @BeforeEach
    void setUp() {
        Member creator = Member.builder()
                .userId("creatorUser")
                .password(passwordEncoder.encode("creatorPassword"))
                .nickName("creatorNickName")
                .email("creator@test.com")
                .phone("010-1111-1111")
                .memberGrade(MemberGrade.GENERAL)
                .profileImage(defaultProfileImage)
                .build();
        creator = memberRepository.save(creator);
        creatorId = creator.getId();

        Member participant = Member.builder()
                .userId("participantUser")
                .password(passwordEncoder.encode("participantPassword"))
                .nickName("participantNickName")
                .email("participant@test.com")
                .phone("010-2222-2222")
                .memberGrade(MemberGrade.GENERAL)
                .profileImage(defaultProfileImage)
                .build();
        participant = memberRepository.save(participant);
        participantId = participant.getId();

        participantToken = jwtUtil.generateToken("testUser", MemberGrade.GENERAL, participantId);

        ChatRoom chatRoom = ChatRoom.builder()
                .title("Test Group Chat")
                .maxParticipants(10)
                .chatRoomType(ChatRoomType.GROUP)
                .member(creator)
                .password(null)
                .build();
        chatRoom = chatRoomRepository.save(chatRoom);
        chatRoomId = chatRoom.getId();

        ChatMember creatorChatMember = ChatMember.builder()
                .chatRoom(chatRoom)
                .member(creator)
                .build();
        chatMemberRepository.save(creatorChatMember);
    }

    @AfterEach
    void tearDown() {
        chatMemberRepository.deleteAll();
        chatRoomRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("채팅방 참여 성공 테스트")
    void joinChatRoomSuccessTest() throws Exception {
        // When
        MvcResult result = mockMvc.perform(post("/chat/join/" + chatRoomId)
                        .header("Authorization", "Bearer " + participantToken)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = result.getResponse().getContentAsString();
        ApiResponse<Long> response = objectMapper.readValue(jsonResponse, new TypeReference<ApiResponse<Long>>() {
        });

        assertThat(response.getResponse()).isNotNull();
        assertThat(response.getResponse()).isEqualTo(participantId);

        Optional<ChatRoom> joinedChatRoom = chatRoomRepository.findById(chatRoomId);
        assertThat(joinedChatRoom).isPresent();

        ChatRoom chatRoom = joinedChatRoom.get();
        List<ChatMember> chatMembers = chatMemberRepository.findByChatRoomAndDeletedAtIsNull(chatRoom);

        assertThat(chatMembers).hasSize(2);
        assertThat(chatMembers).extracting("member.id")
                .contains(creatorId, participantId);
    }
}
