package com.kustacks.kuring.worker.updater.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class UpdaterFirebaseClient {

    private final FirebaseMessaging firebaseMessaging;

    private static final String UPDATER_FIREBASE_APP_NAME = "updater-firebase-app";

    UpdaterFirebaseClient(@Value("${firebase.file-path}") String filePath) throws IOException {

        ClassPathResource resource = new ClassPathResource(filePath);

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                .build();

        FirebaseApp firebaseApp = FirebaseApp.initializeApp(options, UPDATER_FIREBASE_APP_NAME);
        this.firebaseMessaging = FirebaseMessaging.getInstance(firebaseApp);
    }

    public void verifyToken(String token) throws FirebaseMessagingException {

        Message message = Message.builder()
                .setToken(token)
                .build();

        firebaseMessaging.send(message);
    }
}
