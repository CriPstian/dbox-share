package com.cripstian.controllers;

import com.dropbox.core.*;
import com.dropbox.core.util.LangUtil;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@Controller
public class DropboxController {

    private final String key;
    private final String secret;

    public DropboxController(@Value("${dropbox.key}") final String key,
                             @Value("${dropbox.secret}") final String secret) {
        this.key = key;
        this.secret = secret;
    }

    @RequestMapping(value = "/dropbox-authenticate", method = RequestMethod.POST)
    public void authenticate(final HttpServletRequest httpServletRequest,
                               final HttpServletResponse httpServletResponse)
            throws IOException, ServletException {
        final DbxWebAuth.Request request = DbxWebAuth.newRequestBuilder()
                .withRedirectUri(getUrl(httpServletRequest, "/dropbox-auth-finish"),
                        getSessionStore(httpServletRequest))
                .build();

        final String authorizeUrl = getWebAuth(httpServletRequest).authorize(request);

        httpServletResponse.sendRedirect(authorizeUrl);
    }

    @RequestMapping(value = "/dropbox-auth-finish", method = RequestMethod.GET)
    public void finalizeAuthentication(final HttpServletRequest request,
                                       final HttpServletResponse response) throws IOException {
        DbxAuthFinish authFinish = null;

        try {
            authFinish = getWebAuth(request)
                    .finishFromRedirect(
                            getUrl(request, "/dropbox-auth-finish"),
                            getSessionStore(request),
                            request.getParameterMap()
                    );
        } catch(DbxWebAuth.BadRequestException e) {
            e.printStackTrace();
        } catch(DbxException e) {
            e.printStackTrace();
        } catch(DbxWebAuth.ProviderException e) {
            e.printStackTrace();
        } catch(DbxWebAuth.NotApprovedException e) {
            e.printStackTrace();
        } catch(DbxWebAuth.CsrfException e) {
            e.printStackTrace();
        } catch(DbxWebAuth.BadStateException e) {
            e.printStackTrace();
        }

        final String accessToken = authFinish.getAccessToken();
        final HttpSession session = request.getSession(true);
        session.setAttribute("dropboxToken", accessToken);
        response.sendRedirect("/");
    }

    private DbxAppInfo getDbxAppInfo() {
        return new DbxAppInfo(key, secret);

    }

    private DbxSessionStore getSessionStore(final HttpServletRequest request) {
        // Select a spot in the session for DbxWebAuth to store the CSRF token.
        final HttpSession session = request.getSession(true);
        final String sessionKey = "dropbox-auth-csrf-token";
        return new DbxStandardSessionStore(session, sessionKey);
    }

    private String getUrl(final HttpServletRequest request, final String path) {
        URL requestUrl;
        try {
            requestUrl = new URL(request.getRequestURL().toString());
            return new URL(requestUrl, path).toExternalForm();
        } catch (MalformedURLException ex) {
            throw LangUtil.mkAssert("Bad URL", ex);
        }
    }

    private DbxWebAuth getWebAuth(final HttpServletRequest request) {
        return new DbxWebAuth(requestConfig(), getDbxAppInfo());
    }

    @Bean
    public DbxRequestConfig requestConfig() {
        return DbxRequestConfig.newBuilder("example-web-file-browser")
                .build();
    }
}
