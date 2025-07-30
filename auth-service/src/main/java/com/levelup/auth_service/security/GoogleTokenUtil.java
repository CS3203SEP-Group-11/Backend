package com.levelup.auth_service.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.levelup.auth_service.exception.AuthException;
import io.jsonwebtoken.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;
import java.util.Collections;

@Component
@Slf4j
public class GoogleTokenUtil {

    @Value("${google.clientId}")
    private String clientId;

    private GoogleIdToken verify(String idTokenString) throws GeneralSecurityException, IOException, java.io.IOException {
        NetHttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = Utils.getDefaultJsonFactory();

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(clientId))
                .build();

        log.info("Verifying Google ID Token: {}",verifier);
        return verifier.verify(idTokenString);
    }

    public GoogleIdToken.Payload getTokenPayload(String idTokenString) throws AuthException {
        GoogleIdToken idToken;
        try {
            idToken = verify(idTokenString);
        } catch (Exception e) {
            throw new AuthException("Failed to verify Google ID token");
        }

        if (idToken == null) {
            throw new AuthException("Invalid or expired Google ID token.");
        }

        return idToken.getPayload();
    }
}
