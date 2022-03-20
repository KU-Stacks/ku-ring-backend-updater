package com.kustacks.kuring.worker.updater.deptinfo.science;

import com.kustacks.kuring.worker.updater.deptinfo.NoticeScrapInfo;
import com.kustacks.kuring.worker.updater.deptinfo.StaffScrapInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ChemicalsDept extends ScienceCollege {
    public ChemicalsDept() {
        super();

        List<String> pfForumIds = new ArrayList<>(1);
        pfForumIds.add("8900");

        List<String> forumIds = new ArrayList<>(1);
        forumIds.add("8897");

        List<String> boardSeqs = new ArrayList<>(0);

        List<String> menuSeqs = new ArrayList<>(0);

        this.staffScrapInfo = new StaffScrapInfo(pfForumIds);
        this.noticeScrapInfo = new NoticeScrapInfo(forumIds, "CHEMI", boardSeqs, menuSeqs);
        this.code = "121261";
        this.deptName = "화학과";
    }
}
