package flab.Linkedlog.service;

import flab.Linkedlog.dto.member.LogInRequest;
import flab.Linkedlog.dto.member.MyPageResponse;
import flab.Linkedlog.dto.member.SignUpRequest;
import flab.Linkedlog.entity.Member;
import flab.Linkedlog.repository.MemberRepository;
import flab.Linkedlog.util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final S3Service s3Service;

    @Value("${profile.default-image-url}")
    private String defaultProfileImage;

    // 회원가입
    public Long signUp(SignUpRequest signUpDto, MultipartFile profileImage) throws IOException {

        String userId = signUpDto.getUserId();
        String rawPassword = signUpDto.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        String nickname = signUpDto.getNickname();
        String email = signUpDto.getEmail1() + "@" + signUpDto.getEmail2();
        String phone = signUpDto.getPhone1() + "-" +
                signUpDto.getPhone2() + "-" + signUpDto.getPhone3();

        Member member = Member.builder()
                .userId(userId)
                .password(encodedPassword)
                .nickName(nickname)
                .email(email)
                .phone(phone)
                .build();

        if (profileImage != null && !profileImage.isEmpty()) {
            String imageKey = s3Service.uploadFile(profileImage);
            member.storeProfileImage(imageKey);
        } else {
            member.storeProfileImage(defaultProfileImage);
        }


        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();

    }

    public void validateDuplicateMember(Member member) {
        Optional<Member> findMember = memberRepository.findByUserId(member.getUserId());
        if (findMember.isPresent()) {
            throw new IllegalStateException("User Already Exists.");
        }
    }

    // 로그인
    public String login(LogInRequest logInRequest) {

        String userId = logInRequest.getUserId();
        String password = logInRequest.getPassword();
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BadCredentialsException("User Not Found") {
                });

        if (member == null) {
            throw new BadCredentialsException("User not found") {
            };
        }

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new BadCredentialsException("Invalid credentials") {
            };
        }

        return jwtUtil.generateToken(member.getUserId(), member.getMemberGrade(), member.getId());
    }

    // 마이페이지
    @Transactional(readOnly = true)
    public Optional<MyPageResponse> getMyPageById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));
        String profileImageUrl = s3Service.getFileUrl(member.getProfileImage());

        return Optional.of(new MyPageResponse(
                profileImageUrl,
                member.getNickName()
        ));
    }


}



