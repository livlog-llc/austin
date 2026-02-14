package jp.livlog.austin.service;

import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import org.json.JSONObject;

import jakarta.servlet.http.HttpServletRequest;
import jp.livlog.austin.data.Provider;
import jp.livlog.austin.data.Result;
import jp.livlog.austin.data.Setting;
import jp.livlog.austin.share.InfBaseService;
import jp.livlog.austin.share.ProviderType;
import lombok.extern.slf4j.Slf4j;

/**
 * GitHubサービス.
 *
 * @author H.Aoshima
 * @version 1.0
 *
 */
@Slf4j
public class GithubService implements InfBaseService {

    /** インスタンス. */
    private static GithubService instance = new GithubService();

    /**
     * インスタンス取得メソッド.
     * @return インスタンス
     */
    public static GithubService getInstance() {

        return GithubService.instance;
    }


    @Override
    public String auth(final Setting setting, final String appKey, final HttpServletRequest request) throws Exception {

        Provider githubProvider = null;
        for (final Provider provider : setting.getProviders()) {
            if (ProviderType.GITHUB.name.equals(provider.getProviderName()) && provider.getAppKey().equals(appKey)) {
                githubProvider = provider;
                break;
            }
        }

        if (githubProvider == null) {
            throw new Exception("Could not get the provider.");
        }

        final var service = new ServiceBuilder(githubProvider.getClientId())
                .apiSecret(githubProvider.getClientSecret())
                .callback(this.getCallback(appKey, request))
                .defaultScope(githubProvider.getScope())
                .build(GitHubApi.instance());

        final var authorizationUrl = service.getAuthorizationUrl();

        GithubService.log.info(authorizationUrl);
        request.getSession().setAttribute("service", service);

        return authorizationUrl;
    }


    @Override
    public String getCallback(final String appKey, final HttpServletRequest request) {

        final var callbackURL = request.getRequestURL().toString();

        return callbackURL.replace("oauth", "callback");
    }


    @Override
    public Result callback(final Setting setting, final String appKey, final HttpServletRequest request) throws Exception {

        Provider githubProvider = null;
        for (final Provider provider : setting.getProviders()) {
            if (ProviderType.GITHUB.name.equals(provider.getProviderName()) && provider.getAppKey().equals(appKey)) {
                githubProvider = provider;
                break;
            }
        }

        if (githubProvider == null) {
            throw new Exception("Could not get the provider.");
        }

        final var result = new Result();

        final var service = (OAuth20Service) request.getSession().getAttribute("service");

        final var code = request.getParameter("code");

        final var accessToken = service.getAccessToken(code);
        request.getSession().removeAttribute("service");

        final var oauthToken = accessToken.getAccessToken();

        final var oauthRequest = new OAuthRequest(Verb.GET, "https://api.github.com/user");
        service.signRequest(accessToken, oauthRequest);
        try (var response = service.execute(oauthRequest)) {
            if (!response.isSuccessful()) {
                throw new Exception("Failed to fetch user info");
            }

            final var jsonResponse = new JSONObject(response.getBody());
            final var userId = jsonResponse.get("id").toString();

            result.setId(userId);
            result.setOauthToken(oauthToken);
        }

        return result;
    }

}
