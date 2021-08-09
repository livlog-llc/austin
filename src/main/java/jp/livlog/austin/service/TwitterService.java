package jp.livlog.austin.service;

import javax.servlet.http.HttpServletRequest;

import com.github.redouane59.twitter.TwitterClient;
import com.github.redouane59.twitter.signature.TwitterCredentials;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;

import jp.livlog.austin.data.Provider;
import jp.livlog.austin.data.Result;
import jp.livlog.austin.data.Setting;
import jp.livlog.austin.share.InfBaseService;
import jp.livlog.austin.share.ProviderType;
import lombok.extern.slf4j.Slf4j;

/**
 * Twitterサービス.
 *
 * @author H.Aoshima
 * @version 1.0
 *
 */
@Slf4j
public class TwitterService implements InfBaseService {

    /** インスタンス. */
    private static TwitterService instance = new TwitterService();

    /**
     * インスタンス取得メソッド.
     * @return インスタンス
     */
    public static TwitterService getInstance() {

        return TwitterService.instance;
    }


    @Override
    public String auth(Setting setting, String appKey, HttpServletRequest request) throws Exception {

        Provider twitterProvider = null;
        for (final Provider provider : setting.getProviders()) {
            if (ProviderType.TWITTER.name.equals(provider.getProviderName()) && provider.getAppKey().equals(appKey)) {
                twitterProvider = provider;
                break;
            }
        }

        if (twitterProvider == null) {
            throw new Exception("Could not get the provider.");
        }

        final var service = new ServiceBuilder(twitterProvider.getClientId())
                .apiSecret(twitterProvider.getClientSecret())
                .callback(this.getCallback(appKey, request))
                .build(TwitterApi.instance());

        final var requestToken = service.getRequestToken();

        TwitterService.log.info(service.getAuthorizationUrl(requestToken));
        request.getSession().setAttribute("service", service);
        request.getSession().setAttribute("requestToken", requestToken);

        return service.getAuthorizationUrl(requestToken);
    }


    @Override
    public String getCallback(String appKey, HttpServletRequest request) {

        final var callbackURL = request.getRequestURL().toString();

        return callbackURL.replace("oauth", "callback");
    }


    @Override
    public Result callback(Setting setting, String appKey, HttpServletRequest request) throws Exception {

        Provider twitterProvider = null;
        for (final Provider provider : setting.getProviders()) {
            if (ProviderType.TWITTER.name.equals(provider.getProviderName()) && provider.getAppKey().equals(appKey)) {
                twitterProvider = provider;
                break;
            }
        }

        final var result = new Result();

        final var service = (OAuth10aService) request.getSession().getAttribute("service");
        final var requestToken = (OAuth1RequestToken) request.getSession().getAttribute("requestToken");
        final var oauthVerifier = request.getParameter("oauth_verifier");

        // アクセストークンの取得
        final var accessToken = service.getAccessToken(requestToken, oauthVerifier);
        request.getSession().removeAttribute("service");
        request.getSession().removeAttribute("requestToken");

        final var twitterClient = new TwitterClient(TwitterCredentials.builder()
                .accessToken(accessToken.getToken())
                .accessTokenSecret(accessToken.getTokenSecret())
                .apiKey(twitterProvider.getClientId())
                .apiSecretKey(twitterProvider.getClientSecret())
                .build());

        final var id = twitterClient.getUserIdFromAccessToken();
        final var oauthToken = accessToken.getToken();
        final var oauthTokenSecret = accessToken.getTokenSecret();
        result.setId(String.valueOf(id));
        result.setOauthToken(oauthToken);
        result.setOauthTokenSecret(oauthTokenSecret);

        return result;
    }

}
