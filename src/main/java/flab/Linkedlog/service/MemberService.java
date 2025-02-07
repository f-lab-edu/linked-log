package flab.Linkedlog.service;

import flab.Linkedlog.dto.MainResponse;
import flab.Linkedlog.dto.member.LogInRequest;
import flab.Linkedlog.dto.member.MyPageResponse;
import flab.Linkedlog.dto.member.SignUpRequest;
import flab.Linkedlog.entity.Member;
import flab.Linkedlog.repository.MemberRepository;
import flab.Linkedlog.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ImageUploadService imageUploadService;
    private final S3Service s3Service;

    @Value("${profile.default-image-url}")
    private String defaultProfileImage;

    @Value("${profile.default-folder-path}")
    private String defaultProfilepath;

    @Value("${aws.s3.bucket}")
    private String bucketName;

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
                .profileImage(defaultProfilepath + defaultProfileImage)
                .build();

        validateDuplicateMember(member);
        memberRepository.save(member);

        if (profileImage != null && !profileImage.isEmpty()) {
            // async
            CompletableFuture<String> uploadFuture = imageUploadService.uploadProfileImageAsync(profileImage);
            uploadFuture.thenAccept(imageUrl -> {
                member.storeProfileImage(imageUrl);
                memberRepository.save(member);
            }).exceptionally(e -> {
                throw new RuntimeException();
            });
        }

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

    public MainResponse getMain(Long id) {

        Member member = memberRepository.findById(id).orElseThrow();
        String nickName = member.getNickName();

        return MainResponse.builder()
                .id(id)
                .nickname(nickName)
                .build();

    }

    public MainResponse getMainForGuest() {
        return MainResponse.builder()
                .id(null)
                .nickname(null)
                .message("환영합니다")
                .build();
    }


    @Transactional(readOnly = true)
    public MyPageResponse getMyPageById(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        String profileImageUrl = s3Service.getFileUrl(member.getProfileImage(), bucketName);

        return MyPageResponse.builder()
                .memberId(memberId)
                .nickname(member.getNickName())
                .profileImageKey(profileImageUrl)
                .point(member.getCashPoint())
                .build();

    }


}



