package com.kustacks.kuring.worker.updater.util.converter;

import com.kustacks.kuring.worker.updater.staff.dto.StaffDTO;
import com.kustacks.kuring.worker.persistence.staff.Staff;
import org.springframework.stereotype.Component;

@Component
public class StaffEntityToStaffDTOConverter implements DTOConverter<StaffDTO, Staff> {

    @Override
    public StaffDTO convert(Staff target) {
        return StaffDTO.builder()
                .name(target.getName())
                .major(target.getMajor())
                .lab(target.getLab())
                .phone(target.getPhone())
                .email(target.getEmail())
                .deptName(target.getDept())
                .collegeName(target.getCollege()).build();
    }
}
