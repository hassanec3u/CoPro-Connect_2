package com.copro.connect.dto;

import com.copro.connect.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    
    private String token;
    private UserInfo user;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String id;
        private String username;
        private String name;
        private String role;
        
        public static UserInfo fromUser(User user) {
            return new UserInfo(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getRole()
            );
        }
    }
}
