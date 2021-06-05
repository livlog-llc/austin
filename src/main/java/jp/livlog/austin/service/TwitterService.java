package jp.livlog.austin.service;

import javax.servlet.http.HttpServletRequest;

import jp.livlog.austin.data.Provider;
import jp.livlog.austin.data.Setting;
import jp.livlog.austin.share.InfBaseService;
import jp.livlog.austin.share.ProviderType;
import lombok.extern.slf4j.Slf4j;
import twitter4j.TwitterFactory;
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

        final var requestToken = twitter.getOAuthRequestToken(this.getCallback(appKey, request));

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
    public String callback(Setting setting, String appKey) {

        // val twitter = this.getSessionAttribute(SessionAttributeKey.TWITTER_INSTANCE) as Twitter
        // val requestToken = this.getSessionAttribute(SessionAttributeKey.TWITTER_TOKEN) as RequestToken
        //
        // var callbackURL = request.getRequestURL()
        // val index = callbackURL.lastIndexOf("/")
        // callbackURL.replace(index, callbackURL.length, "").append("/callback")
        //
        // // アクセストークンの取得
        // val accessToken = twitter.getOAuthAccessToken(requestToken, oauthVerifier)
        //
        // val twitterId = accessToken.getUserId()
        // val twitterToken = accessToken.getToken()
        // val twitterTokenSecret = accessToken.getTokenSecret()
        //
        //
        // val settingFlg = this.removeSessionAttribute(SessionAttributeKey.SETTING_FLG) as Boolean?
        // if (settingFlg != null && settingFlg) {
        // val redirectUrl = this.removeSessionAttribute(SessionAttributeKey.REDIRECT_URL)
        // // ログインチェック
        // this.loginCheck(model)
        //
        // val check = this.loginAuthService.userService.findByTwitterId(twitterId.toString())
        // if (check != null) {
        // this.setRedirectNotification(
        // redirectAttributes,
        // NotificationCode.NOTICE_ERROR,
        // "TwitterAuthController.twitterCallback.error"
        // )
        // return "redirect:" + redirectUrl
        // }
        //
        // val userDto = requireNotNull(this.userInfo)
        // userDto.twitterId = twitterId.toString()
        // this.loginAuthService.userService.update(userDto)
        // val userDto2 = this.loginAuthService.userService.findById(requireNotNull(userDto.id))
        //
        // this.setSessionAttribute(SessionAttributeKey.LOGIN_SESSION, requireNotNull(userDto2))
        //
        // return "redirect:" + redirectUrl
        // }
        //
        // // セッションにIDとトークンを設定
        // this.setSessionAttribute(SessionAttributeKey.USER_ID, "twitter|" + twitterId)
        // this.setSessionAttribute(SessionAttributeKey.USER_SNS_TOKEN, twitterToken + "|" + twitterTokenSecret)
        //
        // this.setSessionAttribute(SessionAttributeKey.LOGIN_TYPE, LoginType.TWITTER)

        return "redirect:/callback";
    }

}
