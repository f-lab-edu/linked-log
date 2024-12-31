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
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class LeaveGroupChatTest {

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
    private Long participant1Id;
    private Long participant2Id;
    private Long participant3Id;
    private Long chatRoomId;
    private String participant1Token;
    private String participant2Token;
    private String participant3Token;

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

        Member participant1 = Member.builder()
                .userId("participant1User")
                .password(passwordEncoder.encode("participant1Password"))
                .nickName("participant1NickName")
                .email("participant1@test.com")
                .phone("010-1111-1112")
                .memberGrade(MemberGrade.GENERAL)
                .profileImage(defaultProfileImage)
                .build();
        participant1 = memberRepository.save(participant1);
        participant1Id = participant1.getId();
        participant1Token = jwtUtil.generateToken("testUser1", MemberGrade.GENERAL, participant1Id);

        Member participant2 = Member.builder()
                .userId("participant2User")
                .password(passwordEncoder.encode("participant2Password"))
                .nickName("participant2NickName")
                .email("participant2@test.com")
                .phone("010-1111-1113")
                .memberGrade(MemberGrade.GENERAL)
                .profileImage(defaultProfileImage)
                .build();
        participant2 = memberRepository.save(participant2);
        participant2Id = participant2.getId();
        participant2Token = jwtUtil.generateToken("testUser2", MemberGrade.GENERAL, participant2Id);

        Member participant3 = Member.builder()
                .userId("participant3User")
                .password(passwordEncoder.encode("participant3Password"))
                .nickName("participant3NickName")
                .email("participant3@test.com")
                .phone("010-1111-1114")
                .memberGrade(MemberGrade.GENERAL)
                .profileImage(defaultProfileImage)
                .build();
        participant3 = memberRepository.save(participant3);
        participant3Id = participant3.getId();
        participant3Token = jwtUtil.generateToken("testUser3", MemberGrade.GENERAL, participant3Id);

        ChatRoom chatRoom = ChatRoom.builder()
                .title("Test Group Chat")
                .maxParticipants(5)
                .chatRoomType(ChatRoomType.GROUP)
                .member(creator)
                .password(null)
                .build();
        chatRoom = chatRoomRepository.save(chatRoom);
        chatRoomId = chatRoom.getId();

        chatMemberRepository.save(ChatMember.builder().chatRoom(chatRoom).member(creator).build());
        chatMemberRepository.save(ChatMember.builder().chatRoom(chatRoom).member(participant1).build());
        chatMemberRepository.save(ChatMember.builder().chatRoom(chatRoom).member(participant2).build());
        chatMemberRepository.save(ChatMember.builder().chatRoom(chatRoom).member(participant3).build());
    }

    @AfterEach
    void tearDown() {
        chatMemberRepository.deleteAll();
        chatRoomRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("채팅방 퇴장 성공 테스트")
    void leaveChatRoomSuccessTest() throws Exception {
        // When
        MvcResult result = mockMvc.perform(post("/chat/chatroom/" + chatRoomId + "/leave")
                        .header("Authorization", "Bearer " + participant2Token)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = result.getResponse().getContentAsString();
        ApiResponse<Long> response = objectMapper.readValue(jsonResponse, new TypeReference<ApiResponse<Long>>() {
        });

        assertThat(response.getResponse()).isNotNull();
        assertThat(response.getResponse()).isEqualTo(participant2Id);

        Optional<ChatRoom> leftChatRoom = chatRoomRepository.findById(chatRoomId);
        assertThat(leftChatRoom).isPresent();

        ChatRoom chatRoom = leftChatRoom.get();
        List<ChatMember> remainingMembers = chatMemberRepository.findByChatRoomAndDeletedAtIsNull(chatRoom);

        assertThat(remainingMembers).hasSize(3);
        assertThat(remainingMembers).extracting("member.id")
                .contains(creatorId, participant1Id, participant3Id);
    }
}
