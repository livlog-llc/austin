package jp.livlog.austin.resource;

import java.util.HashMap;
import java.util.Map;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Status;

import jp.livlog.austin.repositories.Sql2oConfig;
import jp.livlog.austin.repositories.TemporaryRepository;
import jp.livlog.austin.share.AbsBaseResource;
import lombok.extern.slf4j.Slf4j;

/**
 * Resource which has only one representation.
 */
@Slf4j
public class ResultResource extends AbsBaseResource {

    @Get
    public Representation doGet() throws Exception {

        try {
            final var setting = this.getSetting();

            final var restletRequest = this.getRequest();
            final var servletRequest = ServletUtils.getRequest(restletRequest);
            final var restletResponse = this.getResponse();
            // final var servletResponse = ServletUtils.getResponse(restletResponse);
            restletResponse.setAccessControlAllowOrigin("*");
            final var serverSideRequest = this.isServerSideRequest(restletRequest.getOriginalRef().getQueryAsForm());
            if (!serverSideRequest) {
                final var referer = servletRequest.getHeader("REFERER");
                final var refererValue = referer == null ? "" : referer;
                var domainFlg = true;
                for (final String domain : setting.getDomains()) {
                    if (refererValue.contains(domain)) {
                        domainFlg = false;
                    }
                }
                if (domainFlg) {
                    throw new NotspecifiedDomainError("Not the specified domain.");
                }
            }

            final var attrMap = this.getRequestAttributes();
            final var uuid = (String) attrMap.get("uuid");

            final Map <String, Object> ret = new HashMap <>();

            try (var conn = new Sql2oConfig(servletRequest.getServletContext()).getSql2o().open()) {

                final var temporaryRepository = new TemporaryRepository(conn);
                final var model = temporaryRepository.findById(uuid);
                if (model != null) {
                    ret.put("result", model);
                    ret.put("status", "ok");
                    temporaryRepository.delete(model);
                } else {
                    ret.put("status", "ng");
                }
            }

            return new JsonRepresentation(ret);
        } catch (final Exception e) {
            ResultResource.log.error(e.getMessage(), e);
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
