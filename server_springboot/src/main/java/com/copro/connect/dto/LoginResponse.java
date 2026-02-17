package com.copro.connect.dto;

import com.copro.connect.model.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    private String token;
    private UserInfo user;

    @JsonProperty("mfa_required")
    private Boolean mfaRequired;

    @JsonProperty("masked_email")
    private String maskedEmail;

    private String message;

    /**
     * Réponse classique (login complet, pas de MFA)
     */
    public static LoginResponse success(String token, User user) {
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(UserInfo.fromUser(user));
        response.setMfaRequired(false);
        return response;
    }

    /**
     * Réponse MFA requise (pas de token encore)
     */
    public static LoginResponse mfaRequired(String maskedEmail) {
        LoginResponse response = new LoginResponse();
        response.setMfaRequired(true);
        response.setMaskedEmail(maskedEmail);
        response.setMessage("Un code de vérification a été envoyé à votre adresse email.");
        return response;
    }

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
