package jp.livlog.austin.service;

import javax.servlet.http.HttpServletRequest;

import com.github.scribejava.apis.TrelloApi;
import com.github.scribejava.core.builder.ServiceBuilder;

import jp.livlog.austin.data.Provider;
import jp.livlog.austin.data.Result;
import jp.livlog.austin.data.Setting;
import jp.livlog.austin.share.InfBaseService;
import jp.livlog.austin.share.Parameters;
import jp.livlog.austin.share.ProviderType;
import lombok.extern.slf4j.Slf4j;

/**
 * Trelloサービス.
 *
 * @author H.Aoshima
 * @version 1.0
 *
 */
@Slf4j
public class TrelloService implements InfBaseService {

    /** インスタンス. */
    private static TrelloService instance = new TrelloService();

    /**
     * インスタンス取得メソッド.
     * @return インスタンス
     */
    public static TrelloService getInstance() {

        return TrelloService.instance;
    }


    @Override
    public String auth(Setting setting, String appKey, HttpServletRequest request) throws Exception {

        Provider trelloProvider = null;
        for (final Provider provider : setting.getProviders()) {
            if (ProviderType.TRELLO.name.equals(provider.getProviderName()) && provider.getAppKey().equals(appKey)) {
                trelloProvider = provider;
                break;
            }
        }

        if (trelloProvider == null) {
            throw new Exception("Could not get the provider.");
        }

        final var service = new ServiceBuilder(trelloProvider.getClientId())
                .apiSecret(trelloProvider.getClientSecret())
                .callback(this.getCallback(appKey, request))
                .build(TrelloApi.instance());

        final var requestToken = service.getRequestToken();

        final var parameters = new Parameters();
        parameters.addParameter("scope", "read,write");
        parameters.addParameter("response_type", "fragment");
        parameters.addParameter("expiration", "never");
        parameters.addParameter("name", trelloProvider.getAppName());
        final var authorizationUrl = service.getAuthorizationUrl(requestToken) + "&" + parameters.toQueryString(false);
        TrelloService.log.info(authorizationUrl);

        return authorizationUrl;
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
            if (ProviderType.TRELLO.name.equals(provider.getProviderName()) && provider.getAppKey().equals(appKey)) {
                lineProvider = provider;
                break;
            }
        }

        if (lineProvider == null) {
            throw new Exception("Could not get the provider.");
        }

        final var oauthToken = request.getParameter("oauth_token");

        result.setOauthToken(oauthToken);

        return result;
    }

}
