package com.kustacks.kuring.worker.updater.util.converter;

import com.kustacks.kuring.worker.updater.staff.dto.StaffDTO;
import com.kustacks.kuring.worker.persistence.staff.Staff;
import org.springframework.stereotype.Component;

@Component
public class StaffDTOToStaffEntityConverter implements DTOConverter<Staff, StaffDTO> {

    @Override
    public Staff convert(StaffDTO target) {
        return Staff.builder()
                .name(target.getName())
                .major(target.getMajor())
                .lab(target.getLab())
                .phone(target.getPhone())
                .email(target.getEmail())
                .dept(target.getDeptName())
                .college(target.getCollegeName()).build();
    }
}
