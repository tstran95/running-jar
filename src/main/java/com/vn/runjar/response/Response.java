package com.vn.runjar.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vn.runjar.constant.Constant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Response {
    private String code;
    private String description;
    private String message;
    private String tokenId;

    public static Response getResponse(String code, String description , String message ,String tokenId) {
        return Response.builder()
                        .code(code)
                        .description(description)
                        .message(message)
                        .tokenId(tokenId)
                        .build();
    }

    @JsonIgnore
    public static Response responseError(String tokenId) {
        return Response.builder()
                .code(Constant.ERROR)
                .description(Constant.FAIL)
                .message(null)
                .tokenId(tokenId)
                .build();
    }
}
