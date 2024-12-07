package flab.Linkedlog.config;

public class CustomUserDetails {
    private final Long memberId;
    private final String username;

    public CustomUserDetails(Long memberId, String username) {
        this.memberId = memberId;
        this.username = username;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getUsername() {
        return username;
    }
}
