package jp.livlog.austin.service;

import java.util.UUID;

import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.base64.Base64;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.github.scribejava.core.pkce.PKCE;
import com.github.scribejava.core.pkce.PKCECodeChallengeMethod;
import com.google.gson.Gson;
import com.twitter.clientlib.auth.TwitterOAuth20Service;

import jakarta.servlet.http.HttpServletRequest;
import jp.livlog.austin.data.Provider;
import jp.livlog.austin.data.Result;
import jp.livlog.austin.data.Setting;
import jp.livlog.austin.model.TemporaryModel;
import jp.livlog.austin.repositories.Sql2oConfig;
import jp.livlog.austin.repositories.TemporaryRepository;
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
    private static TwitterService instance    = new TwitterService();

    /** 空文字. */
    public static final String    API_VER_2_0 = "2.0";

    /**
     * インスタンス取得メソッド.
     * @return インスタンス
     */
    public static TwitterService getInstance() {

        return TwitterService.instance;
    }


    @Override
    public String auth(final Setting setting, final String appKey, final HttpServletRequest request) throws Exception {

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

        if (TwitterService.API_VER_2_0.equals(twitterProvider.getApiVersion())) {
            return this.processOAuth20Auth(twitterProvider, appKey, request);
        } else {
            return this.processOAuth11Auth(twitterProvider, appKey, request);
        }
    }


    @Override
    public String getCallback(final String appKey, final HttpServletRequest request) {

        final var callbackURL = request.getRequestURL().toString();

        return callbackURL.replace("oauth", "callback");
    }


    @Override
    public Result callback(final Setting setting, final String appKey, final HttpServletRequest request) throws Exception {

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

        if (TwitterService.API_VER_2_0.equals(twitterProvider.getApiVersion())) {
            return this.processOAuth20Callback(twitterProvider, appKey, request);
        } else {
            return this.processOAuth11Callback(twitterProvider, appKey, request);
        }
    }


    private String processOAuth11Auth(final Provider twitterProvider, final String appKey, final HttpServletRequest request) throws Exception {

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


    private String processOAuth20Auth(final Provider twitterProvider, final String appKey, final HttpServletRequest request) {

        final var key = request.getParameter("key");

        final var service = new TwitterOAuth20Service(
                twitterProvider.getClientId(),
                twitterProvider.getClientSecret(),
                this.getCallback(appKey, request),
                twitterProvider.getScope());

        final var state = UUID.randomUUID().toString();
        final var pkce = new PKCE();
        pkce.setCodeChallenge("challenge");
        pkce.setCodeChallengeMethod(PKCECodeChallengeMethod.PLAIN);
        pkce.setCodeVerifier("challenge");

        request.getSession().setAttribute("key", key);
        request.getSession().setAttribute("service", service);
        request.getSession().setAttribute("state", state);
        request.getSession().setAttribute("pkce", pkce);
        TwitterService.log.info(service.getAuthorizationUrl(pkce, state));

        return service.getAuthorizationUrl(pkce, state);
    }


    private Result processOAuth11Callback(final Provider twitterProvider, final String appKey, final HttpServletRequest request) throws Exception {

        final var result = new Result();

        final var service = (OAuth10aService) request.getSession().getAttribute("service");
        final var requestToken = (OAuth1RequestToken) request.getSession().getAttribute("requestToken");
        final var oauthVerifier = request.getParameter("oauth_verifier");

        // アクセストークンの取得
        final var accessToken = service.getAccessToken(requestToken, oauthVerifier);
        request.getSession().removeAttribute("service");
        request.getSession().removeAttribute("requestToken");

        final var oauthToken = accessToken.getToken();
        final var oauthTokenSecret = accessToken.getTokenSecret();

        result.setId(this.getUserIdFromAccessToken(oauthToken));
        result.setOauthToken(oauthToken);
        result.setOauthTokenSecret(oauthTokenSecret);

        return result;
    }


    private Result processOAuth20Callback(final Provider twitterProvider, final String appKey, final HttpServletRequest request) throws Exception {

        final var result = new Result();

        final var key = (String) request.getSession().getAttribute("key");
        final var service = (TwitterOAuth20Service) request.getSession().getAttribute("service");
        final var pkce = (PKCE) request.getSession().getAttribute("pkce");
        final var checkState = (String) request.getSession().getAttribute("state");
        final var code = request.getParameter("code");
        final var state = request.getParameter("state");

        if (!checkState.equals(state)) {
            throw new Exception("Cross-site request forgery.");
        }

        // アクセストークンの取得
        final var accessToken = service.getAccessToken(pkce, code);
        request.getSession().removeAttribute("service");
        request.getSession().removeAttribute("pkce");
        request.getSession().removeAttribute("state");

        final var oauthToken = accessToken.getAccessToken();
        final var rawResponse = accessToken.getRawResponse();

        TwitterService.log.info(rawResponse);

        result.setOauthToken(oauthToken);
        result.setOther(Base64.encode(rawResponse.getBytes()));

        try (var conn = new Sql2oConfig(request.getServletContext()).getSql2o().open()) {

            final var gson = new Gson();
            final var json = gson.toJson(result);

            final var model = new TemporaryModel();
            model.setKey(key);
            model.setValue(json);
            final var temporaryRepository = new TemporaryRepository(conn);
            temporaryRepository.insert(model);
        }

        return result;
    }


    private String getUserIdFromAccessToken(final String accessToken) {

        if (accessToken == null
                || accessToken.isEmpty()
                || !accessToken.contains("-")) {
            throw new IllegalArgumentException("Access token null, empty or incorrect");
        }
        return accessToken.substring(0, accessToken.indexOf("-"));
    }
}
