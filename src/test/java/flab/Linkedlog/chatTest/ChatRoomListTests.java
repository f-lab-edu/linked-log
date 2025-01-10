package flab.Linkedlog.chatTest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.chat.ChatRoomListResponse;
import flab.Linkedlog.entity.ChatMember;
import flab.Linkedlog.entity.ChatRoom;
import flab.Linkedlog.entity.Member;
import flab.Linkedlog.entity.enums.ChatRoomType;
import flab.Linkedlog.entity.enums.MemberGrade;
import flab.Linkedlog.repository.MemberRepository;
import flab.Linkedlog.repository.chat.ChatMemberRepository;
import flab.Linkedlog.repository.chat.ChatRoomRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class ChatRoomListTests {

    @Autowired
    private MockMvc mockMvc;

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

    @Value("${profile.default-image-url}")
    private String defaultProfileImage;

    @BeforeEach
    void setUp() {
        Member creator1 = Member.builder()
                .userId("creator1")
                .password(passwordEncoder.encode("password1"))
                .nickName("creatorNick1")
                .email("creator1@test.com")
                .phone("010-1111-1111")
                .memberGrade(MemberGrade.GENERAL)
                .profileImage(defaultProfileImage)
                .build();
        creator1 = memberRepository.save(creator1);

        Member creator2 = Member.builder()
                .userId("creator2")
                .password(passwordEncoder.encode("password2"))
                .nickName("creatorNick2")
                .email("creator2@test.com")
                .phone("010-2222-2222")
                .memberGrade(MemberGrade.GENERAL)
                .profileImage(defaultProfileImage)
                .build();
        creator2 = memberRepository.save(creator2);

        ChatRoom chatRoom1 = ChatRoom.builder()
                .title("First Chat Room")
                .maxParticipants(5)
                .chatRoomType(ChatRoomType.GROUP)
                .member(creator1)
                .password(null)
                .build();
        chatRoom1 = chatRoomRepository.save(chatRoom1);

        ChatRoom chatRoom2 = ChatRoom.builder()
                .title("Second Chat Room")
                .maxParticipants(7)
                .chatRoomType(ChatRoomType.GROUP)
                .member(creator2)
                .password("securePassword")
                .build();
        chatRoom2 = chatRoomRepository.save(chatRoom2);

        for (int i = 1; i <= 2; i++) {
            Member participant = Member.builder()
                    .userId("participant" + i)
                    .password(passwordEncoder.encode("password" + i))
                    .nickName("participantNick" + i)
                    .email("participant" + i + "@test.com")
                    .phone("010-3333-33" + i)
                    .memberGrade(MemberGrade.GENERAL)
                    .profileImage(defaultProfileImage)
                    .build();
            participant = memberRepository.save(participant);

            ChatMember chatMember = ChatMember.builder()
                    .chatRoom(chatRoom1)
                    .member(participant)
                    .build();
            chatMemberRepository.save(chatMember);
        }

        for (int i = 3; i <= 5; i++) {
            Member participant = Member.builder()
                    .userId("participant" + i)
                    .password(passwordEncoder.encode("password" + i))
                    .nickName("participantNick" + i)
                    .email("participant" + i + "@test.com")
                    .phone("010-4444-44" + i)
                    .memberGrade(MemberGrade.GENERAL)
                    .profileImage(defaultProfileImage)
                    .build();
            participant = memberRepository.save(participant);

            ChatMember chatMember = ChatMember.builder()
                    .chatRoom(chatRoom2)
                    .member(participant)
                    .build();
            chatMemberRepository.save(chatMember);
        }
    }

    @AfterEach
    void tearDown() {
        chatMemberRepository.deleteAll();
        chatRoomRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("채팅방 목록 조회 테스트")
    void getChatRoomsListTest() throws Exception {
        // When
        MvcResult result = mockMvc.perform(get("/chat/chatroom")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = result.getResponse().getContentAsString();
        ApiResponse<List<ChatRoomListResponse>> response =
                objectMapper.readValue(jsonResponse, new TypeReference<>() {
                });

        assertThat(response.getResponse()).isNotNull();
        assertThat(response.getResponse()).hasSize(2);

        List<ChatRoomListResponse> chatRooms = response.getResponse();

        ChatRoomListResponse chatRoom1 = chatRooms.stream()
                .filter(room -> room.getTitle().equals("First Chat Room"))
                .findFirst()
                .orElseThrow();
        assertThat(chatRoom1.getMaxParticipants()).isEqualTo(5);
        assertThat(chatRoom1.isHasPassword()).isFalse();

        ChatRoomListResponse chatRoom2 = chatRooms.stream()
                .filter(room -> room.getTitle().equals("Second Chat Room"))
                .findFirst()
                .orElseThrow();
        assertThat(chatRoom2.getMaxParticipants()).isEqualTo(7);
        assertThat(chatRoom2.isHasPassword()).isTrue();
    }

}
