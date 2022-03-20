package com.kustacks.kuring.worker.updater.api.scrap.parser;

import com.kustacks.kuring.worker.error.InternalLogicException;
import org.jsoup.nodes.Document;

import java.util.List;

public interface HTMLParser {

    List<String[]> parse(Document document) throws InternalLogicException;
}
