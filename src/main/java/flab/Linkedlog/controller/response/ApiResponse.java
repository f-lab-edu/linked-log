package flab.Linkedlog.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private T response;
    private String error;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .response(data)
                .error(null)
                .build();
    }

    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return ApiResponse.<T>builder()
                .response(null)
                .error(errorCode)
                .build();
    }
}