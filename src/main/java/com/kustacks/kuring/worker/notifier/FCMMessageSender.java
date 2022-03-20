package com.kustacks.kuring.worker.notifier;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.kustacks.kuring.worker.error.ErrorCode;
import com.kustacks.kuring.worker.error.InternalLogicException;
import com.kustacks.kuring.worker.notifier.firebase.dto.AdminFBMessageDTO;
import com.kustacks.kuring.worker.notifier.mq.dto.AdminMQMessageDTO;
import com.kustacks.kuring.worker.notifier.mq.dto.NewNoticeMQMessageDTO;
import com.kustacks.kuring.worker.notifier.firebase.NotifierFirebaseClient;
import com.kustacks.kuring.worker.notifier.firebase.dto.NoticeFBMessageDTO;
import com.kustacks.kuring.worker.notifier.util.converter.DTOConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FCMMessageSender {

    private final NotifierFirebaseClient notifierFirebaseClient;
    private final DTOConverter<NoticeFBMessageDTO, NewNoticeMQMessageDTO> newNoticeMQMessageDTOToNoticeMessageDTOConverter;
    private final DTOConverter<AdminFBMessageDTO, AdminMQMessageDTO> adminMQMessageDTOToAdminFBMessageDTOConverter;

    public FCMMessageSender(NotifierFirebaseClient notifierFirebaseClient,
                            DTOConverter<NoticeFBMessageDTO, NewNoticeMQMessageDTO> newNoticeMQMessageDTOToNoticeMessageDTOConverter,
                            DTOConverter<AdminFBMessageDTO, AdminMQMessageDTO> adminMQMessageDTOToAdminFBMessageDTOConverter
    ) {
        this.notifierFirebaseClient = notifierFirebaseClient;
        this.newNoticeMQMessageDTOToNoticeMessageDTOConverter = newNoticeMQMessageDTOToNoticeMessageDTOConverter;
        this.adminMQMessageDTOToAdminFBMessageDTOConverter = adminMQMessageDTOToAdminFBMessageDTOConverter;
    }

    public void send(NewNoticeMQMessageDTO messageDTO) {
        try {
            String token = messageDTO.getToken();

            NoticeFBMessageDTO fcmMessage = newNoticeMQMessageDTOToNoticeMessageDTOConverter.convert(messageDTO);
            if(token == null) {
                notifierFirebaseClient.sendMessage(fcmMessage);
            } else {
                notifierFirebaseClient.sendMessage(token, fcmMessage);
            }

            log.info("notice FCM 메세지를 성공적으로 전송했습니다.");
            log.info("{} {} {} {}", fcmMessage.getCategory(), fcmMessage.getArticleId(), fcmMessage.getPostedDate(), fcmMessage.getSubject());
        } catch (FirebaseMessagingException e) {
            throw new InternalLogicException(ErrorCode.FB_FAIL_SEND);
        }
    }

    public void send(AdminMQMessageDTO messageDTO) {
        try {
            String token = messageDTO.getToken();

            AdminFBMessageDTO fcmMessage = adminMQMessageDTOToAdminFBMessageDTOConverter.convert(messageDTO);
            if(token == null) {
                notifierFirebaseClient.sendMessage(fcmMessage);
            } else {
                notifierFirebaseClient.sendMessage(token, fcmMessage);
            }

            log.info("admin FCM 메세지를 성공적으로 전송했습니다.");
            log.info("{} {}", fcmMessage.getTitle(), fcmMessage.getBody());
        } catch (FirebaseMessagingException e) {
            throw new InternalLogicException(ErrorCode.FB_FAIL_SEND);
        }
    }
}
