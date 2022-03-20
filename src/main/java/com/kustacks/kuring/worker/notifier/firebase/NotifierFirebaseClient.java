package com.kustacks.kuring.worker.notifier.firebase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.kustacks.kuring.worker.notifier.firebase.dto.AdminFBMessageDTO;
import com.kustacks.kuring.worker.notifier.firebase.dto.NoticeFBMessageDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class NotifierFirebaseClient {

    @Value("${server.deploy.environment}")
    private String deployEnv;

    private final String DEV_SUFFIX = ".dev";

    private final FirebaseMessaging firebaseMessaging;
    private final ObjectMapper objectMapper;

    private static final String NOTIFIER_FIREBASE_APP_NAME = "notifier-firebase-app";

    NotifierFirebaseClient(ObjectMapper objectMapper, @Value("${firebase.file-path}") String filePath) throws IOException {

        this.objectMapper = objectMapper;

        ClassPathResource resource = new ClassPathResource(filePath);

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                .build();

        FirebaseApp firebaseApp = FirebaseApp.initializeApp(options, NOTIFIER_FIREBASE_APP_NAME);
        this.firebaseMessaging = FirebaseMessaging.getInstance(firebaseApp);
    }

    /**
     * Firebase message에는 두 가지 paylaad가 존재한다.
     * 1. notification
     * 2. data
     *
     * notification을 Message로 만들어 보내면 여기서 설정한 title, body가 직접 앱 noti로 뜬다.
     * data로 Message를 만들어 보내면 이것을 앱 클라이언트(Andriod)가 받아서, 가공한 뒤 푸쉬 알람으로 만들 수 있다.
     *
     * 따라서 여기선 putData를 사용하여 보내고, 클라이언트가 푸쉬 알람을 만들어 띄운다.
     *
     * @param messageDTO
     * @throws FirebaseMessagingException
     */
    public void sendMessage(NoticeFBMessageDTO messageDTO) throws FirebaseMessagingException {

        Map<String, String> noticeMap = objectMapper.convertValue(messageDTO, Map.class);

        StringBuilder topic = new StringBuilder(messageDTO.getCategory());
        if(deployEnv.equals("dev") || deployEnv.equals("local")) {
            topic.append(DEV_SUFFIX);
        }

        Message newMessage = Message.builder()
                .putAllData(noticeMap)
                .setTopic(topic.toString())
                .build();

        firebaseMessaging.send(newMessage);
    }

    public void sendMessage(AdminFBMessageDTO messageDTO) throws FirebaseMessagingException {

        Map<String, String> noticeMap = objectMapper.convertValue(messageDTO, Map.class);

        StringBuilder topic = new StringBuilder("admin");
        if(deployEnv.equals("dev") || deployEnv.equals("local")) {
            topic.append(DEV_SUFFIX);
        }

        Message newMessage = Message.builder()
                .putAllData(noticeMap)
                .setTopic(topic.toString())
                .build();

        firebaseMessaging.send(newMessage);
    }

    public void sendMessage(List<NoticeFBMessageDTO> messageDTOList) throws FirebaseMessagingException {
        for (NoticeFBMessageDTO messageDTO : messageDTOList) {
            sendMessage(messageDTO);
        }
    }

    public void sendMessage(String token, NoticeFBMessageDTO messageDTO) throws FirebaseMessagingException {

        Map<String, String> messageMap = objectMapper.convertValue(messageDTO, Map.class);

        Message newMessage = Message.builder()
                .putAllData(messageMap)
                .setToken(token)
                .build();

        firebaseMessaging.send(newMessage);
    }

    public void sendMessage(String token, AdminFBMessageDTO messageDTO) throws FirebaseMessagingException {

        Map<String, String> messageMap = objectMapper.convertValue(messageDTO, Map.class);

        Message newMessage = Message.builder()
                .putAllData(messageMap)
                .setToken(token)
                .build();

        firebaseMessaging.send(newMessage);
    }
}
