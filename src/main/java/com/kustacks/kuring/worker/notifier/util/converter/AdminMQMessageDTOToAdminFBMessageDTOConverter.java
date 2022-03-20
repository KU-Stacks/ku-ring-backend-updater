package com.kustacks.kuring.worker.notifier.util.converter;

import com.kustacks.kuring.worker.notifier.firebase.dto.AdminFBMessageDTO;
import com.kustacks.kuring.worker.notifier.mq.dto.AdminMQMessageDTO;
import org.springframework.stereotype.Component;

@Component
public class AdminMQMessageDTOToAdminFBMessageDTOConverter implements DTOConverter<AdminFBMessageDTO, AdminMQMessageDTO> {

    @Override
    public AdminFBMessageDTO convert(AdminMQMessageDTO target) {
        return AdminFBMessageDTO.builder()
                .title(target.getTitle())
                .body(target.getBody())
                .build();
    }
}
