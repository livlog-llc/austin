package jp.livlog.austin.service;

import javax.servlet.http.HttpServletRequest;

import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.restfb.DefaultFacebookClient;
import com.restfb.Version;
import com.restfb.types.User;

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

        final var service = new ServiceBuilder(facebookProvider.getClientId())
                .apiSecret(facebookProvider.getClientSecret())
                .callback(this.getCallback(appKey, request))
                .defaultScope(facebookProvider.getScope())
                .build(FacebookApi.instance());

        FacebookService.log.info(service.getAuthorizationUrl());
        request.getSession().setAttribute("service", service);

        return service.getAuthorizationUrl();
    }


    @Override
    public String getCallback(String appKey, HttpServletRequest request) {

        var callbackURL = request.getRequestURL().toString();
        callbackURL = callbackURL.replace("oauth", "callback");

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

        final var service = (OAuth20Service) request.getSession().getAttribute("service");

        final var code = request.getParameter("code");

        // アクセストークンの取得
        final var accessToken = service.getAccessToken(code);
        request.getSession().removeAttribute("service");

        // Facebook IDを取得する
        final var facebookClient = new DefaultFacebookClient(accessToken.getAccessToken(),
                Version.getVersionFromString(facebookProvider.getApiVersion()));
        final var user = facebookClient.fetchObject("me", User.class);
        final var id = user.getId();

        result.setId(id);
        result.setOauthToken(accessToken.getAccessToken());

        return result;
    }

}
