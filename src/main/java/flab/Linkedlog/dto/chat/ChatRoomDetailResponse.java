package flab.Linkedlog.dto.chat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class ChatRoomDetailResponse {

    @NotNull(message = "채팅방 아이디")
    private Long chatRoomId;

    @NotEmpty(message = "채팅방 이름")
    private String title;

    @NotNull(message = "사용자 정보")
    private List<ChatRoomMemberListResponse> joinedUserInfo;

    @NotNull(message = "채팅방 아이디")
    private Set<Long> connectedUserIds;

    @NotNull(message = "채팅방 아이디")
    private List<ChatMessageResponse> recentMessages;
}
