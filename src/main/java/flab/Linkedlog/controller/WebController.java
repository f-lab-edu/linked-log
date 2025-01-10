package flab.Linkedlog.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/front")
public class WebController {

    Logger logger = LoggerFactory.getLogger(WebController.class);

    @GetMapping("/main")
    public String mainPage() {
        return "main";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String signUpPage() {
        return "signup";
    }

    @GetMapping("/chat/chatroom")
    public String chatRoomListPage() {
        logger.info("chat room list page 접근");
        return "chatroomlist";
    }

    @GetMapping("/chat/create/group/form")
    public String chatRoomCreatePage() {
        logger.info("채팅방 생성 버튼");
        return "chatroomcreate";
    }

    @GetMapping("/chat/chatroom/{chatRoomId}/info")
    public String chatRoomDetailPage(@PathVariable Long chatRoomId) {
        logger.info("채팅방 참여중");
        return "chatroomdetail";
    }


}
