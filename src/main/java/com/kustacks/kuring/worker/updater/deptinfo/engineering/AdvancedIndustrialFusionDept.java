package com.kustacks.kuring.worker.updater.deptinfo.engineering;

import com.kustacks.kuring.worker.updater.deptinfo.NoticeScrapInfo;
import com.kustacks.kuring.worker.updater.deptinfo.StaffScrapInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AdvancedIndustrialFusionDept extends EngineeringCollege {

    public AdvancedIndustrialFusionDept() {
        super();

        List<String> pfForumIds = new ArrayList<>(1);
        pfForumIds.add("4113024");

        List<String> forumIds = new ArrayList<>(0);

        List<String> boardSeqs = new ArrayList<>(1);
        boardSeqs.add("1109");

        List<String> menuSeqs = new ArrayList<>(1);
        menuSeqs.add("7794");

        this.staffScrapInfo = new StaffScrapInfo(pfForumIds);
        this.noticeScrapInfo = new NoticeScrapInfo(forumIds, "AIF", boardSeqs, menuSeqs);
        this.code = "127431";
        this.deptName = "신산업융합학과";
    }
}
