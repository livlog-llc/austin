package jp.livlog.austin.service;

import com.github.scribejava.apis.DiscordApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;

import jakarta.servlet.http.HttpServletRequest;
import jp.livlog.austin.data.Provider;
import jp.livlog.austin.data.Result;
import jp.livlog.austin.data.Setting;
import jp.livlog.austin.helper.discord.DiscordUserClient;
import jp.livlog.austin.share.InfBaseService;
import jp.livlog.austin.share.ProviderType;
import lombok.extern.slf4j.Slf4j;

/**
 * Discordサービス.
 *
 * @author H.Aoshima
 * @version 1.0
 *
 */
@Slf4j
public class DiscordService implements InfBaseService {

    /** インスタンス. */
    private static DiscordService instance = new DiscordService();

    /**
     * インスタンス取得メソッド.
     * @return インスタンス
     */
    public static DiscordService getInstance() {

        return DiscordService.instance;
    }


    @Override
    public String auth(final Setting setting, final String appKey, final HttpServletRequest request) throws Exception {

        Provider discordProvider = null;
        for (final Provider provider : setting.getProviders()) {
            if (ProviderType.DISCORD.name.equals(provider.getProviderName()) && provider.getAppKey().equals(appKey)) {
                discordProvider = provider;
                break;
            }
        }

        if (discordProvider == null) {
            throw new Exception("Could not get the provider.");
        }

        final var service = new ServiceBuilder(discordProvider.getClientId())
                .apiSecret(discordProvider.getClientSecret())
                .callback(this.getCallback(appKey, request))
                .defaultScope(discordProvider.getScope())
                .build(DiscordApi.instance());

        final var authorizationUrl = service.getAuthorizationUrl();

        DiscordService.log.info(authorizationUrl);
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

        Provider discordProvider = null;
        for (final Provider provider : setting.getProviders()) {
            if (ProviderType.DISCORD.name.equals(provider.getProviderName()) && provider.getAppKey().equals(appKey)) {
                discordProvider = provider;
                break;
            }
        }

        if (discordProvider == null) {
            throw new Exception("Could not get the provider.");
        }

        final var result = new Result();

        final var service = (OAuth20Service) request.getSession().getAttribute("service");

        final var code = request.getParameter("code");

        // アクセストークンの取得
        final var accessToken = service.getAccessToken(code);
        request.getSession().removeAttribute("service");

        final var oauthToken = accessToken.getAccessToken();
        final var rawResponse = accessToken.getRawResponse();

        DiscordService.log.info(rawResponse);

        final var client = new DiscordUserClient(oauthToken);
        final var user = client.getUserInfo();

        result.setId(user.getId());
        result.setOauthToken(oauthToken);

        return result;
    }

}
