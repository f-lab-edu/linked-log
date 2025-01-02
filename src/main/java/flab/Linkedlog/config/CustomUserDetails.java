package flab.Linkedlog.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CustomUserDetails {
    private final Long memberId;
    private final String username;
}
