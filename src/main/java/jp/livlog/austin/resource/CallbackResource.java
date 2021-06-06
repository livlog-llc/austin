package jp.livlog.austin.resource;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.restlet.data.Reference;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import jp.livlog.austin.data.Result;
import jp.livlog.austin.share.AbsBaseResource;
import jp.livlog.austin.share.ProviderType;
import lombok.extern.slf4j.Slf4j;

/**
 * Resource which has only one representation.
 */
@Slf4j
public class CallbackResource extends AbsBaseResource {

    /** クッキーの保存期間(秒). */
    private static final int COOKIE_PRESERVATION_PERIOD = 60; // １分

    @Get
    public Representation doGet() throws Exception {

        try {
            final var setting = this.getSetting();

            final var restletRequest = this.getRequest();
            final var servletRequest = ServletUtils.getRequest(restletRequest);
            final var restletResponse = this.getResponse();
            final var servletResponse = ServletUtils.getResponse(restletResponse);

            final var attrMap = this.getRequestAttributes();
            final var provider = (String) attrMap.get("provider");
            final var appKey = (String) attrMap.get("app_key");

            try {
                Result result = null;
                switch (ProviderType.getType(provider)) {
                    case TWITTER:
                        result = this.twitterService.callback(setting, appKey, servletRequest);
                        break;
                    case FACEBOOK:
                        result = this.facebookService.callback(setting, appKey, servletRequest);
                        break;
                }

                this.cookieScope("austin-status", "ok", servletResponse);
                this.cookieScope("austin-provider", provider, servletResponse);
                this.cookieScope("austin-id", result.getId(), servletResponse);
                this.cookieScope("austin-oauth-token", result.getOauthToken(), servletResponse);
                this.cookieScope("austin-oauth-token-secret", result.getOauthTokenSecret(), servletResponse);
            } catch (final Exception e) {
                CallbackResource.log.error(e.getMessage(), e);
                this.cookieScope("austin-status", "ng", servletResponse);
                this.cookieScope("austin-error-message", "authentication_failure", servletResponse);
            }

            var callbackURL = servletRequest.getRequestURL().toString();
            final var index = callbackURL.indexOf("callback");
            callbackURL = callbackURL.substring(0, index) + "app/index.html";

            final var newRef = new Reference(callbackURL);
            this.redirectSeeOther(newRef);
            return new EmptyRepresentation();
        } catch (final Exception e) {
            CallbackResource.log.error(e.getMessage(), e);
            throw e;
        }

    }


    /**
     * @param name CharSequence
     * @param value String
     * @param response HttpServletResponse
     * @throws Exception 例外
     */
    public void cookieScope(CharSequence name, String value, HttpServletResponse response) throws Exception {

        if (name == null) {
            throw new NullPointerException("The name parameter must not be null.");
        } else {

            final var cookie = new Cookie(name.toString(), value);
            cookie.setMaxAge(CallbackResource.COOKIE_PRESERVATION_PERIOD);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
    }
}