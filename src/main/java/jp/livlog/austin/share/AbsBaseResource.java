package jp.livlog.austin.share;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.restlet.resource.ServerResource;

import com.google.gson.Gson;

import jp.livlog.austin.data.Setting;
import jp.livlog.austin.service.FacebookService;
import jp.livlog.austin.service.LineService;
import jp.livlog.austin.service.TrelloService;
import jp.livlog.austin.service.TwitterService;

/**
 * ベース用リソースクラス.
 *
 * @author H.Aoshima
 * @version 1.0
 */
public abstract class AbsBaseResource extends ServerResource {

    /** TwitterService. */
    protected final TwitterService  twitterService  = TwitterService.getInstance();

    /** FacebookService. */
    protected final FacebookService facebookService = FacebookService.getInstance();

    /** LineService. */
    protected final LineService     lineService     = LineService.getInstance();

    /** TrelloService. */
    protected final TrelloService   trelloService   = TrelloService.getInstance();

    protected Setting getSetting() throws IOException {

        final var application = (ServletContext) this.getContext().getAttributes().get(Symbol.SERVLET_CONTEXT);
        if (application == null) {
            final var json = this.getFile(Symbol.SETTING_PATH);
            final var gson = new Gson();
            final var model = gson.fromJson(json, Setting.class);
            return model;
        }

        var setting = (Setting) application.getAttribute(Symbol.AUSTIN_SETTING);
        if (setting == null) {
            final var json = this.getFile(Symbol.SETTING_PATH);
            final var gson = new Gson();
            setting = gson.fromJson(json, Setting.class);
            application.setAttribute(Symbol.AUSTIN_SETTING, setting);
        }

        return setting;
    }


    protected String getFile(String path) throws IOException {

        final Reader reader = new InputStreamReader(AbsBaseResource.class.getResourceAsStream(path));

        return IOUtils.toString(reader);
    }
}