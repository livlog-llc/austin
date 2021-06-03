package jp.livlog.austin.resource;

import java.io.IOException;

import org.restlet.data.Reference;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import jp.livlog.austin.data.Setting;
import jp.livlog.austin.share.AbsAustinResource;
import lombok.extern.slf4j.Slf4j;

/**
 * Resource which has only one representation.
 */
@Slf4j
public class OAuthResource extends AbsAustinResource {

    @Get
    public Representation doGet() throws IOException {

        Setting setting = getSetting();

        OAuthResource.log.info(setting.toString());

        // final var id = (String) this.getRequest().getAttributes().get("id");
        final var newRef = new Reference("https://www.cotogoto.ai/");
        this.redirectSeeOther(newRef);
        return new EmptyRepresentation();
    }
}