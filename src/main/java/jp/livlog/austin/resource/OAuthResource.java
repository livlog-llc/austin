package jp.livlog.austin.resource;

import org.restlet.data.Reference;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import jp.livlog.austin.share.AbsBaseResource;
import jp.livlog.austin.share.ProviderType;
import lombok.extern.slf4j.Slf4j;

/**
 * Resource which has only one representation.
 */
@Slf4j
public class OAuthResource extends AbsBaseResource {

    @Get
    public Representation doGet() throws Exception {

        try {
            final var setting = this.getSetting();

            final var restletRequest = this.getRequest();
            final var servletRequest = ServletUtils.getRequest(restletRequest);
            final var ipAddress = servletRequest.getRemoteAddr();

            final var attrMap = this.getRequestAttributes();
            final var provider = (String) attrMap.get("provider");
            final var appKey = (String) attrMap.get("app_key");

            String uriReference = null;
            switch (ProviderType.getType(provider)) {
                case TWITTER:
                    uriReference = this.twitterService.auth(setting, appKey, servletRequest);
                    break;
                case FACEBOOK:
                    break;
            }

            OAuthResource.log.info(uriReference);

            final var newRef = new Reference(uriReference);
            this.redirectSeeOther(newRef);
            return new EmptyRepresentation();
        } catch (final Exception e) {
            OAuthResource.log.error(e.getMessage(), e);
            throw new Exception(e);
        }

    }
}