package jp.livlog.austin.service;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;

import jp.livlog.austin.data.Provider;
import jp.livlog.austin.data.Result;
import jp.livlog.austin.data.Setting;
import jp.livlog.austin.helper.line.Profile;
import jp.livlog.austin.share.InfBaseService;
import jp.livlog.austin.share.Parameters;
import jp.livlog.austin.share.ProviderType;
import lombok.extern.slf4j.Slf4j;

/**
 * Lineサービス.
 *
 * @author H.Aoshima
 * @version 1.0
 *
 */
@Slf4j
public class LineService implements InfBaseService {

    /** インスタンス. */
    private static LineService instance = new LineService();

    /**
     * インスタンス取得メソッド.
     * @return インスタンス
     */
    public static LineService getInstance() {

        return LineService.instance;
    }


    // https://developers.line.biz/ja/docs/line-login/integrate-line-login/#making-an-authorization-request
    @Override
    public String auth(Setting setting, String appKey, HttpServletRequest request) throws Exception {

        Provider lineProvider = null;
        for (final Provider provider : setting.getProviders()) {
            if (ProviderType.LINE.name.equals(provider.getProviderName()) && provider.getAppKey().equals(appKey)) {
                lineProvider = provider;
                break;
            }
        }

        if (lineProvider == null) {
            throw new Exception("Could not get the provider.");
        }

        final var state = UUID.randomUUID().toString();
        request.getSession().setAttribute("state", state);

        final var parameters = new Parameters();
        parameters.addParameter("response_type", "code");
        parameters.addParameter("client_id", lineProvider.getClientId());
        parameters.addParameter("redirect_uri", this.getCallback(appKey, request));
        parameters.addParameter("state", state);
        parameters.addParameter("scope", lineProvider.getScope());

        final var url = new StringBuffer("https://access.line.me/oauth2/v2.1/authorize");
        url.append(parameters.toString());

        LineService.log.info(url.toString());

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

        Provider lineProvider = null;
        for (final Provider provider : setting.getProviders()) {
            if (ProviderType.LINE.name.equals(provider.getProviderName()) && provider.getAppKey().equals(appKey)) {
                lineProvider = provider;
                break;
            }
        }

        if (lineProvider == null) {
            throw new Exception("Could not get the provider.");
        }

        final var checkState = (String) request.getSession().getAttribute("state");
        final var code = request.getParameter("code");
        final var state = request.getParameter("state");

        if (!checkState.equals(state)) {
            throw new Exception("Cross-site request forgery.");
        }

        // アクセストークンの取得
        final var httpclient = HttpClients.createDefault();
        final var post = new HttpPost("https://api.line.me/oauth2/v2.1/token");
        post.setHeader("Content-Type", "application/x-www-form-urlencoded;");

        final List <NameValuePair> params = new ArrayList <>();
        // grant_type String 必須 client_credentials
        // client_id String 必須 チャネルID。コンソールで確認できます。
        // client_secret String 必須 チャネルシークレット。コンソールで確認できます。
        params.add(new BasicNameValuePair("grant_type", "authorization_code"));
        params.add(new BasicNameValuePair("code", code));
        params.add(new BasicNameValuePair("redirect_uri", this.getCallback(appKey, request)));
        params.add(new BasicNameValuePair("client_id", lineProvider.getClientId()));
        params.add(new BasicNameValuePair("client_secret", lineProvider.getClientSecret()));
        post.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

        final HttpResponse response = httpclient.execute(post);

        final var entity = response.getEntity();

        // 値の取得
        String accessToken = null;
        try (var inputStream = entity.getContent()) {
            final var r = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            /// mapperオブジェクトを作成
            final var gson = new Gson();
            final var map = gson.fromJson(r, Map.class);
            accessToken = (String) map.get("access_token");
        }

        // Line IDを取得する
        final var map = Profile.execute(accessToken);

        result.setId((String) map.get("userId"));
        result.setOauthToken(accessToken);

        return result;
    }

}
