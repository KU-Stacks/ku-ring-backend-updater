package com.kustacks.kuring.worker.updater.util.converter;

import org.springframework.stereotype.Component;

@Component
public class YmdhmsToYmdConverter implements DateConverter<String, String> {

    @Override
    public String convert(String ymdhms) {
        String[] splited = ymdhms.split(" ");
        String ymd = splited[0];
        return ymd.replaceAll("-", "");
    }
}
