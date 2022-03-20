package com.kustacks.kuring.worker.updater.deptinfo;

import lombok.Getter;

import java.util.List;

@Getter
public class StaffScrapInfo extends ScrapInfo {

    private final List<String> pfForumId;

    public StaffScrapInfo(List<String> pfForumIds) {
        this.pfForumId = pfForumIds;
    }
}
