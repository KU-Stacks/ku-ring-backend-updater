package com.kustacks.kuring.worker.updater.deptinfo.education;

import com.kustacks.kuring.worker.updater.deptinfo.NoticeScrapInfo;
import com.kustacks.kuring.worker.updater.deptinfo.StaffScrapInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MusicEducationDept extends EducationCollege {

    public MusicEducationDept() {
        super();

        List<String> pfForumIds = new ArrayList<>(1);
        pfForumIds.add("9803");

        List<String> forumIds = new ArrayList<>(1);
        forumIds.add("9801");

        List<String> boardSeqs = new ArrayList<>(0);

        List<String> menuSeqs = new ArrayList<>(0);

        this.staffScrapInfo = new StaffScrapInfo(pfForumIds);
        this.noticeScrapInfo = new NoticeScrapInfo(forumIds, "MUSICEDU", boardSeqs, menuSeqs);
        this.code = "105011";
        this.deptName = "음악교육과";
    }
}
