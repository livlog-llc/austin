package jp.livlog.austin.share;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.restlet.resource.ServerResource;

import com.google.gson.Gson;

import jp.livlog.austin.data.Setting;

/**
 * オースティンリソースクラス.
 *
 * @author H.Aoshima
 * @version 1.0
 */
public abstract class AbsAustinResource extends ServerResource {

    protected Setting getSetting() throws IOException {

        final var application = (ServletContext) this.getContext().getAttributes().get(Symbol.SERVLET_CONTEXT);
        if (application == null) {
            String json = getFile(Symbol.SETTING_PATH);
            Gson gson = new Gson();
            Setting model = gson.fromJson(json, Setting.class);
            return model;
        }

        Setting setting = (Setting) application.getAttribute(Symbol.AUSTIN_SETTING);
        if (setting == null) {
            String json = getFile(Symbol.SETTING_PATH);
            Gson gson = new Gson();
            setting = gson.fromJson(json, Setting.class);
            application.setAttribute(Symbol.AUSTIN_SETTING, setting);
        }

        return setting;
    }


    protected String getFile(String path) throws IOException {

        final Reader reader = new InputStreamReader(AbsAustinResource.class.getResourceAsStream(path));

        return IOUtils.toString(reader);
    }
}