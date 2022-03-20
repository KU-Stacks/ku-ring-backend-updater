package com.kustacks.kuring.worker.persistence.staff;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    default Map<String, Staff> findByDeptContainingMap(List<String> deptNames) {

        Map<String, Staff> staffMap = new HashMap<>();
        for (String deptName : deptNames) {
            List<Staff> staffs = findByDeptContaining(deptName);
            for (Staff staff : staffs) {
                staffMap.putIfAbsent(staff.getEmail(), staff);
            }
        }

        return staffMap;
    }

    List<Staff> findByDeptContaining(String deptName);
}
