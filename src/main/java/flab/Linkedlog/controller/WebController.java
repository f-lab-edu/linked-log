package flab.Linkedlog.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/front")
@Slf4j
public class WebController {

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

    @GetMapping("/mypage")
    public String myPagePage() {
        return "mypage";
    }

    @GetMapping("/chat/chatroom")
    public String chatRoomListPage() {
        log.info("chat room list page 접근");
        return "chatroomlist";
    }

    @GetMapping("/chat/create/group/form")
    public String chatRoomCreatePage() {
        log.info("채팅방 생성 버튼");
        return "chatroomcreate";
    }

    @GetMapping("/chat/chatroom/{chatRoomId}/info")
    public String chatRoomDetailPage(@PathVariable Long chatRoomId) {
        log.info("채팅방 참여중");
        return "chatroomdetail";
    }

//    @GetMapping("/payment/payrequest")
//    public String payRequestPage() {
//        return "payrequest";
//    }

    @GetMapping("/payment/productlist")
    public String productListPage() {
        return "productlist";
    }

    @GetMapping("/payment/widget")
    public String paymentWidgetPage() {
        return "widget";
    }

    @GetMapping("/payment/success")
    public String paymentSuccessPage() {
        return "paysuccess";
    }

    @GetMapping("/payment/fail")
    public String paymentFailPage() {
        return "payfail";
    }
    
}
