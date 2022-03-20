package com.kustacks.kuring.worker.updater.util.converter;

import org.springframework.stereotype.Component;

@Component
public class YyyymmddConverter implements DateConverter<String, String> {

    public YyyymmddConverter() {
    }

    @Override
    public String convert(String date) {
        if(date.length() == 8) {
            return date;
        } else if(date.length() == 6) {
            return "20" + date;
        } else {
            String[] splited = date.split("[-\\.]");
            return splited[0] + splited[1] + splited[2];
        }
    }
}
