package jp.livlog.austin.service;

import javax.servlet.http.HttpServletRequest;

import com.github.scribejava.apis.SlackApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;

import jp.livlog.austin.data.Provider;
import jp.livlog.austin.data.Result;
import jp.livlog.austin.data.Setting;
import jp.livlog.austin.share.InfBaseService;
import jp.livlog.austin.share.ProviderType;
import lombok.extern.slf4j.Slf4j;

/**
 * Slackサービス.
 *
 * @author H.Aoshima
 * @version 1.0
 *
 */
@Slf4j
public class SlackService implements InfBaseService {

    /** インスタンス. */
    private static SlackService instance = new SlackService();

    /**
     * インスタンス取得メソッド.
     * @return インスタンス
     */
    public static SlackService getInstance() {

        return SlackService.instance;
    }


    @Override
    public String auth(Setting setting, String appKey, HttpServletRequest request) throws Exception {

        Provider slackProvider = null;
        for (final Provider provider : setting.getProviders()) {
            if (ProviderType.SLACK.name.equals(provider.getProviderName()) && provider.getAppKey().equals(appKey)) {
                slackProvider = provider;
                break;
            }
        }

        if (slackProvider == null) {
            throw new Exception("Could not get the provider.");
        }

        final var service = new ServiceBuilder(slackProvider.getClientId())
                .apiSecret(slackProvider.getClientSecret())
                .callback(this.getCallback(appKey, request))
                .defaultScope(slackProvider.getScope())
                .build(SlackApi.instance());

        SlackService.log.info(service.getAuthorizationUrl());
        request.getSession().setAttribute("service", service);

        return service.getAuthorizationUrl();
    }


    @Override
    public String getCallback(String appKey, HttpServletRequest request) {

        final var callbackURL = request.getRequestURL().toString();

        return callbackURL.replace("oauth", "callback");
    }


    @Override
    public Result callback(Setting setting, String appKey, HttpServletRequest request) throws Exception {

        Provider slackProvider = null;
        for (final Provider provider : setting.getProviders()) {
            if (ProviderType.SLACK.name.equals(provider.getProviderName()) && provider.getAppKey().equals(appKey)) {
                slackProvider = provider;
                break;
            }
        }

        final var result = new Result();

        final var service = (OAuth20Service) request.getSession().getAttribute("service");

        final var code = request.getParameter("code");

        // アクセストークンの取得
        final var accessToken = service.getAccessToken(code);
        request.getSession().removeAttribute("service");

        String token = accessToken.getAccessToken();
        String rawResponse = accessToken.getRawResponse();

        // final var twitterClient = new TwitterClient(TwitterCredentials.builder()
        // .accessToken(accessToken.getToken())
        // .accessTokenSecret(accessToken.getTokenSecret())
        // .apiKey(slackProvider.getClientId())
        // .apiSecretKey(slackProvider.getClientSecret())
        // .build());
        //
        // final var id = twitterClient.getUserIdFromAccessToken();
        // final var oauthToken = accessToken.getToken();
        // final var oauthTokenSecret = accessToken.getTokenSecret();
        // result.setId(String.valueOf(id));
        // result.setOauthToken(oauthToken);
        // result.setOauthTokenSecret(oauthTokenSecret);

        return result;
    }

}
