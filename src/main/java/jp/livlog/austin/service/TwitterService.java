package jp.livlog.austin.service;

import javax.servlet.http.HttpServletRequest;

import jp.livlog.austin.data.Provider;
import jp.livlog.austin.data.Result;
import jp.livlog.austin.data.Setting;
import jp.livlog.austin.share.InfBaseService;
import jp.livlog.austin.share.ProviderType;
import lombok.extern.slf4j.Slf4j;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

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

        final var conf = new ConfigurationBuilder();
        conf.setOAuthConsumerKey(twitterProvider.getClientId());
        conf.setOAuthConsumerSecret(twitterProvider.getClientSecret());
        final var twitter = new TwitterFactory(conf.build()).getInstance();

        final var callbackURL = this.getCallback(appKey, request);
        TwitterService.log.info(callbackURL);
        final var requestToken = twitter.getOAuthRequestToken(callbackURL);

        TwitterService.log.info(requestToken.getAuthenticationURL());
        request.getSession().setAttribute("twitter", twitter);
        request.getSession().setAttribute("requestToken", requestToken);

        return requestToken.getAuthenticationURL();
    }


    @Override
    public String getCallback(String appKey, HttpServletRequest request) {

        final var callbackURL = request.getRequestURL().toString();

        return callbackURL.replace("oauth", "callback");
    }


    @Override
    public Result callback(Setting setting, String appKey, HttpServletRequest request) throws Exception {

        final var result = new Result();

        final var twitter = (Twitter) request.getSession().getAttribute("twitter");
        final var requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
        final var verifier = request.getParameter("oauth_verifier");

        // アクセストークンの取得
        final var accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
        request.getSession().removeAttribute("twitter");
        request.getSession().removeAttribute("requestToken");

        final var id = accessToken.getUserId();
        final var oauthToken = accessToken.getToken();
        final var oauthTokenSecret = accessToken.getTokenSecret();
        result.setId(String.valueOf(id));
        result.setOauthToken(oauthToken);
        result.setOauthTokenSecret(oauthTokenSecret);

        return result;
    }

}
