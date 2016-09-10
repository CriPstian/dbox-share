package com.cripstian.controllers;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class ProcessFilesController {

    private static final String SLASH = "/";
    private static final String ENCODING = "UTF-8";
    private static final String SPACE_ENCODED = "%20";
    private static final String SPACE_DECODED = " ";
    private static final String PUBLIC_PATH = "/Public/";

    private final DbxRequestConfig requestConfig;

    @Autowired
    public ProcessFilesController(final DbxRequestConfig requestConfig) {
        this.requestConfig = Objects.requireNonNull(requestConfig, "requestConfig must not be null.");
    }

    @RequestMapping(value = "/links", method = RequestMethod.POST)
    public ResponseEntity recieveLinks(@RequestBody final String links,
            final HttpServletRequest request) {
        final List<String> paths = Arrays.stream(links.split("&"))
                .map(this::encodeUri)
                .map(this::escapeSpaces)
                .map(this::getFilePath)
                .collect(Collectors.toList());
        paths.forEach(System.out::println);
        copyToPublic(request, paths);
        return ResponseEntity.ok("Files have been copied with success.");
    }

    private void copyToPublic(final HttpServletRequest request, final List<String> paths) {
        final Object token = request.getSession(true).getAttribute("dropboxToken");
        if(token != null) {
            final DbxClientV2 client = new DbxClientV2(requestConfig, token.toString());
            paths.forEach(filePath -> {
                try {
                    client.files().copy(SLASH + filePath, PUBLIC_PATH + filePath);
                } catch(DbxException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private String encodeUri(final String link) {
        try {
            return URLDecoder.decode(link, ENCODING);
        } catch(UnsupportedEncodingException e) {
            return e.getMessage();
        }
    }

    private String escapeSpaces(final String link) {
        return link.replace(SPACE_ENCODED, SPACE_DECODED);
    }

    private String getFilePath(final String link) {
        return Arrays.stream(link.split(SLASH)).skip(6)
                .collect(Collectors.joining(SLASH));
    }



}
