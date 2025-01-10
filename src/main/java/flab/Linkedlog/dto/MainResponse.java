package flab.Linkedlog.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MainResponse {

    @Nullable
    private Long id;

    @Nullable
    private String nickname;

    @Nullable
    private String message;
}
