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

/**
 * Resource which has only one representation.
 */
@Slf4j
public class CallbackResource extends AbsBaseResource {

    @Get
    public Representation represent() throws Exception {

        final var setting = this.getSetting();

        final var restletRequest = this.getRequest();
        final var servletRequest = ServletUtils.getRequest(restletRequest);

        final var attrMap = this.getRequestAttributes();
        final var provider = (String) attrMap.get("provider");
        final var appKey = (String) attrMap.get("app_key");

        Result result = null;
        switch (ProviderType.getType(provider)) {
            case TWITTER:
                result = this.twitterService.callback(setting, appKey, servletRequest);
                break;
            case FACEBOOK:
                break;
        }

        var callbackURL = servletRequest.getRequestURL().toString();
        final var index = callbackURL.indexOf("callback");
        callbackURL = callbackURL.substring(0, index) + "html/index.html";

        final var newRef = new Reference(callbackURL);
        this.redirectSeeOther(newRef);
        return new EmptyRepresentation();

    }

}