package io.github.jiangbyte.hei.interfaces.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicUserResponse {

    private Long userId;
    private String username;
    private String userType;
}
