package jp.livlog.austin.service;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import com.github.scribejava.apis.GoogleApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import jp.livlog.austin.data.Provider;
import jp.livlog.austin.data.Result;
import jp.livlog.austin.data.Setting;
import jp.livlog.austin.share.InfBaseService;
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
public class GoogleService implements InfBaseService {

    /** インスタンス. */
    private static GoogleService instance = new GoogleService();

    /**
     * インスタンス取得メソッド.
     * @return インスタンス
     */
    public static GoogleService getInstance() {

        return GoogleService.instance;
    }


    @Override
    public String auth(final Setting setting, final String appKey, final HttpServletRequest request) throws Exception {

        Provider googleProvider = null;
        for (final Provider provider : setting.getProviders()) {
            if (ProviderType.GOOGLE.name.equals(provider.getProviderName()) && provider.getAppKey().equals(appKey)) {
                googleProvider = provider;
                break;
            }
        }

        if (googleProvider == null) {
            throw new Exception("Could not get the provider.");
        }

        // 追加パラメータの設定
        final Map <String, String> additionalParams = new HashMap <>();
        additionalParams.put("access_type", "offline");
        additionalParams.put("prompt", "consent");

        final var service = new ServiceBuilder(googleProvider.getClientId())
                .apiSecret(googleProvider.getClientSecret())
                .defaultScope(googleProvider.getScope()) // スコープの設定
                .callback(this.getCallback(appKey, request))
                .build(GoogleApi20.instance());

        final var authorizationUrl = service.createAuthorizationUrlBuilder()
                .additionalParams(additionalParams)
                .build();

        GoogleService.log.info(authorizationUrl);
        request.getSession().setAttribute("service", service);

        return authorizationUrl;
    }


    @Override
    public String getCallback(final String appKey, final HttpServletRequest request) {

        var callbackURL = request.getRequestURL().toString();
        callbackURL = callbackURL.replace("oauth", "callback");

        return callbackURL;
    }


    @Override
    public Result callback(final Setting setting, final String appKey, final HttpServletRequest request) throws Exception {

        final var result = new Result();

        Provider googleProvider = null;
        for (final Provider provider : setting.getProviders()) {
            if (ProviderType.GOOGLE.name.equals(provider.getProviderName()) && provider.getAppKey().equals(appKey)) {
                googleProvider = provider;
                break;
            }
        }

        if (googleProvider == null) {
            throw new Exception("Could not get the provider.");
        }

        final var service = (OAuth20Service) request.getSession().getAttribute("service");

        final var code = request.getParameter("code");

        // アクセストークンの取得
        final var accessToken = service.getAccessToken(code);
        request.getSession().removeAttribute("service");

        // // Facebook IDを取得する
        // final var facebookClient = new DefaultFacebookClient(accessToken.getAccessToken(),
        // Version.getVersionFromString(googleProvider.getApiVersion()));
        // final var user = facebookClient.fetchObject("me", User.class);
        // final var id = user.getId();

        // result.setId(id);
        // result.setOauthToken(accessToken.getAccessToken());

        // ユーザー情報の取得
        final var oauthRequest = new OAuthRequest(Verb.GET, "https://www.googleapis.com/oauth2/v3/userinfo");
        service.signRequest(accessToken, oauthRequest);
        try (var response = service.execute(oauthRequest)) {
            if (!response.isSuccessful()) {
                throw new Exception("Failed to fetch user info");
            }

            final var jsonResponse = new JSONObject(response.getBody());
            final var userId = jsonResponse.getString("sub"); // GoogleのOpenIDはJSONレスポンスの"sub"フィールドに含まれています

            result.setId(userId);
            result.setOauthToken(accessToken.getAccessToken());
        }

        return result;
    }

}
