package jp.livlog.austin.resource;

import org.restlet.data.Reference;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Status;

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
            final var restletResponse = this.getResponse();
            // final var servletResponse = ServletUtils.getResponse(restletResponse);
            restletResponse.setAccessControlAllowOrigin("*");
            final var referer = servletRequest.getHeader("REFERER");
            var domainFlg = true;
            for (final String domain : setting.getDomains()) {
                if (referer.contains(domain)) {
                    domainFlg = false;
                }
            }
            if (domainFlg) {
                throw new NotspecifiedDomainError("Not the specified domain.");
            }

            final var attrMap = this.getRequestAttributes();
            final var provider = (String) attrMap.get("provider");
            final var appKey = (String) attrMap.get("app_key");

            String uriReference = null;
            switch (ProviderType.getType(provider)) {
                case TWITTER:
                    uriReference = this.twitterService.auth(setting, appKey, servletRequest);
                    break;
                case FACEBOOK:
                    uriReference = this.facebookService.auth(setting, appKey, servletRequest);
                    break;
                case LINE:
                    uriReference = this.lineService.auth(setting, appKey, servletRequest);
                    break;
                case TRELLO:
                    uriReference = this.trelloService.auth(setting, appKey, servletRequest);
                    break;
                case SLACK:
                    uriReference = this.slackService.auth(setting, appKey, servletRequest);
                    break;
                case DISCORD:
                    uriReference = this.discordService.auth(setting, appKey, servletRequest);
                    break;
                default:
                    break;
            }

            OAuthResource.log.info(uriReference);

            final var newRef = new Reference(uriReference);
            this.redirectSeeOther(newRef);
            return new EmptyRepresentation();
        } catch (final Exception e) {
            OAuthResource.log.error(e.getMessage(), e);
            throw e;
        }

    }

    @Status (403)
    public class NotspecifiedDomainError extends Exception {

        public NotspecifiedDomainError(final String message) {

            super(message);
        }
    }
}
