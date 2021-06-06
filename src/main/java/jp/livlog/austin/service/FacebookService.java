package jp.livlog.austin.service;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.restfb.DefaultFacebookClient;
import com.restfb.Version;
import com.restfb.types.User;

import jp.livlog.austin.data.Provider;
import jp.livlog.austin.data.Result;
import jp.livlog.austin.data.Setting;
import jp.livlog.austin.share.InfBaseService;
import jp.livlog.austin.share.Parameters;
import jp.livlog.austin.share.ProviderType;
import lombok.extern.slf4j.Slf4j;

/**
 * Facebookサービス.
 *
 * @author H.Aoshima
 * @version 1.0
 *
 */
@Slf4j
public class FacebookService implements InfBaseService {

    /** インスタンス. */
    private static FacebookService instance = new FacebookService();

    /**
     * インスタンス取得メソッド.
     * @return インスタンス
     */
    public static FacebookService getInstance() {

        return FacebookService.instance;
    }


    @Override
    public String auth(Setting setting, String appKey, HttpServletRequest request) throws Exception {

        Provider facebookProvider = null;
        for (final Provider provider : setting.getProviders()) {
            if (ProviderType.FACEBOOK.name.equals(provider.getProviderName()) && provider.getAppKey().equals(appKey)) {
                facebookProvider = provider;
                break;
            }
        }

        if (facebookProvider == null) {
            throw new Exception("Could not get the provider.");
        }

        final var parameters = new Parameters();
        parameters.addParameter("client_id", facebookProvider.getClientId());
        parameters.addParameter("redirect_uri", this.getCallback(appKey, request));
        parameters.addParameter("scope", facebookProvider.getScope());

        final var url = new StringBuffer("https://www.facebook.com/" + facebookProvider.getApiVersion() + "/dialog/oauth");
        url.append(parameters.toString());

        FacebookService.log.info(url.toString());

        return url.toString();
    }


    @Override
    public String getCallback(String appKey, HttpServletRequest request) {

        var callbackURL = request.getRequestURL().toString();
        callbackURL = callbackURL.replace("oauth", "callback");
        // if (callbackURL.contains("https")) {
        // callbackURL = callbackURL.replace("http", "https");
        // }

        return callbackURL;
    }


    @Override
    public Result callback(Setting setting, String appKey, HttpServletRequest request) throws Exception {

        final var result = new Result();

        Provider facebookProvider = null;
        for (final Provider provider : setting.getProviders()) {
            if (ProviderType.FACEBOOK.name.equals(provider.getProviderName()) && provider.getAppKey().equals(appKey)) {
                facebookProvider = provider;
                break;
            }
        }

        if (facebookProvider == null) {
            throw new Exception("Could not get the provider.");
        }

        final var code = request.getParameter("code");

        // アクセストークンの取得
        final var parameters = new Parameters();
        parameters.addParameter("client_id", facebookProvider.getClientId());
        parameters.addParameter("redirect_uri", this.getCallback(appKey, request));
        parameters.addParameter("client_secret", facebookProvider.getClientSecret());
        parameters.addParameter("code", code);

        final var url = new StringBuffer("https://graph.facebook.com/" + facebookProvider.getApiVersion() + "/oauth/access_token");
        url.append(parameters.toString());

        FacebookService.log.info(url.toString());

        final var connection = (HttpURLConnection) new URL(url.toString()).openConnection();

        String accessToken = null;
        try (var inputStream = connection.getInputStream()) {
            final var r = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            final var json = IOUtils.toString(r);
            /// mapperオブジェクトを作成
            final var gson = new Gson();
            final var map = gson.fromJson(json, Map.class);
            accessToken = (String) map.get("access_token");
        }

        // Facebook IDを取得する
        final var facebookClient = new DefaultFacebookClient(accessToken,
                Version.getVersionFromString(facebookProvider.getApiVersion()));
        final var user = facebookClient.fetchObject("me", User.class);
        final var id = user.getId();

        result.setId(id);
        result.setOauthToken(accessToken);

        return result;
    }

}
