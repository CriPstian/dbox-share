package com.cripstian.controllers;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
public class HomeController {

    private static final String LOGGED = "logged";

    private final DbxRequestConfig requestConfig;

    @Autowired
    public HomeController(DbxRequestConfig requestConfig) {
        this.requestConfig = Objects.requireNonNull(requestConfig, "requestConfig must not be null.");
    }

    @RequestMapping(value = "/")
    public String home(final HttpServletRequest request, final Model model) throws DbxException {
        final HttpSession session = request.getSession(true);
        final Object token = session.getAttribute("dropboxToken");
        if(token != null) {
            model.addAttribute(LOGGED, LOGGED);
            final DbxClientV2 client = new DbxClientV2(requestConfig, token.toString());
            FullAccount account = client.users().getCurrentAccount();
            model.addAttribute("userDisplayName", account.getName().getDisplayName());
            model.addAttribute("userProfilePhoto", client.users().getCurrentAccount().getProfilePhotoUrl());
        }
        return "index";
    }

    private List<String> listFilesInPath(final DbxClientV2 client, final String path) {
        final List<String> acc = new ArrayList<>();
        try {
            ListFolderResult listFolderResult = client.files().listFolder(path);
            while (true) {
                listFolderResult.getEntries().stream()
                        .map(Metadata::getPathLower)
                        .forEach(acc::add);

                if (!listFolderResult.getHasMore()) {
                    break;
                }

                listFolderResult = client.files().listFolderContinue(listFolderResult.getCursor());
            }
        } catch(DbxException e) {
            e.printStackTrace();
        }
        return acc;
    }

}
