package com.kustacks.kuring.worker.updater.staff.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter @Setter
@AllArgsConstructor
@Builder
public class StaffDTO {

    private String name;

    private String major;

    private String lab;

    private String phone;

    private String email;

    private String deptName;

    private String collegeName;

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }

        StaffDTO staffDTO = (StaffDTO) o;
        return Objects.equals(staffDTO.getName(), name) && Objects.equals(staffDTO.getMajor(), major) && Objects.equals(staffDTO.getLab(), lab)
                && Objects.equals(staffDTO.getPhone(), phone) && Objects.equals(staffDTO.getEmail(), email) && Objects.equals(staffDTO.getDeptName(), deptName)
                && Objects.equals(staffDTO.getCollegeName(), collegeName);
    }
}
