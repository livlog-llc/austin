package jp.livlog.austin.resource;

import org.restlet.data.Reference;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import jp.livlog.austin.data.Result;
import jp.livlog.austin.share.AbsBaseResource;
import jp.livlog.austin.share.ProviderType;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * Resource which has only one representation.
 */
@Slf4j
public class CallbackResource extends AbsBaseResource {

    @Get
    public Representation doGet() throws Exception {

        try {
            final var setting = this.getSetting();

            final var restletRequest = this.getRequest();
            final var servletRequest = ServletUtils.getRequest(restletRequest);
            final var restletResponse = this.getResponse();
            final var servletResponse = ServletUtils.getResponse(restletResponse);
            restletResponse.setAccessControlAllowOrigin("*");
            final var serverSideRequest = this.isServerSideRequest(restletRequest.getOriginalRef().getQueryAsForm());

            final var attrMap = this.getRequestAttributes();
            final var provider = (String) attrMap.get("provider");
            final var appKey = (String) attrMap.get("app_key");

            Result result = null;
            try {
                switch (Objects.requireNonNull(ProviderType.getType(provider))) {
                    case TWITTER:
                        result = this.twitterService.callback(setting, appKey, servletRequest);
                        break;
                    case FACEBOOK:
                        result = this.facebookService.callback(setting, appKey, servletRequest);
                        break;
                    case LINE:
                        result = this.lineService.callback(setting, appKey, servletRequest);
                        break;
                    case TRELLO:
                        result = this.trelloService.callback(setting, appKey, servletRequest);
                        break;
                    case SLACK:
                        result = this.slackService.callback(setting, appKey, servletRequest);
                        break;
                    case DISCORD:
                        result = this.discordService.callback(setting, appKey, servletRequest);
                        break;
                    case GOOGLE:
                        result = this.googleService.callback(setting, appKey, servletRequest);
                        break;
                    default:
                        break;
                }

                if (!serverSideRequest) {
                    this.cookieScope("austin-status", "ok", servletResponse);
                    this.cookieScope("austin-provider", provider, servletResponse);
                    this.cookieScope("austin-id", result.getId(), servletResponse);
                    this.cookieScope("austin-oauth-token", result.getOauthToken(), servletResponse);
                    this.cookieScope("austin-oauth-token-secret", result.getOauthTokenSecret(), servletResponse);
                    this.cookieScope("austin-other", result.getOther(), servletResponse);
                }
            } catch (final Exception e) {
                CallbackResource.log.error(e.getMessage(), e);
                if (!serverSideRequest) {
                    this.cookieScope("austin-status", "ng", servletResponse);
                    this.cookieScope("austin-error-message", "authentication_failure", servletResponse);
                }
            }

            var callbackURL = (String) servletRequest.getSession().getAttribute("return_url");
            if (callbackURL == null || callbackURL.isEmpty()) {
                callbackURL = servletRequest.getRequestURL().toString();
                final var index = callbackURL.indexOf("callback");
                callbackURL = callbackURL.substring(0, index) + "app/close.html";
            }
            servletRequest.getSession().removeAttribute("return_url");

            final var newRef = new Reference(callbackURL);
            if (serverSideRequest) {
                if (result != null) {
                    newRef.addQueryParameter("austin-status", "ok");
                    this.addQueryParameterIfPresent(newRef, "austin-provider", provider);
                    this.addQueryParameterIfPresent(newRef, "austin-id", result.getId());
                    this.addQueryParameterIfPresent(newRef, "austin-oauth-token", result.getOauthToken());
                    this.addQueryParameterIfPresent(newRef, "austin-oauth-token-secret", result.getOauthTokenSecret());
                    this.addQueryParameterIfPresent(newRef, "austin-other", result.getOther());
                } else {
                    newRef.addQueryParameter("austin-status", "ng");
                    newRef.addQueryParameter("austin-error-message", "authentication_failure");
                }
            }
            this.redirectSeeOther(newRef);
            return new EmptyRepresentation();
        } catch (final Exception e) {
            CallbackResource.log.error(e.getMessage(), e);
            throw e;
        }

    }

    private void addQueryParameterIfPresent(final Reference reference, final String key, final String value) {

        if (value == null || value.isEmpty()) {
            return;
        }
        reference.addQueryParameter(key, value);
    }
}
