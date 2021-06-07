package jp.livlog.austin.helper.line;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

/**
 * プロフィールを取得する.
 *
 * @author H.Aoshima
 * @version 1.0
 *
 */
@Slf4j
public final class Profile {

    /** URI. */
    private static final String SUGGETS_URL = "https://api.line.me/v2/profile";

    /**
     * プロフィールを取得する.
     * @param accessToken アクセストークン
     * @return 結果
     * @throws Exception 例外
     */
    @SuppressWarnings ("rawtypes")
    public static Map execute(final String accessToken) throws Exception {

        Map result = null;

        try {
            final var url = new URL(Profile.SUGGETS_URL);
            final var connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            Profile.log.info(url.toString());

            try (var is = connection.getInputStream(); Reader r = new InputStreamReader(is, StandardCharsets.UTF_8);) {
                final var gson = new Gson();
                result = gson.fromJson(r, Map.class);
            }

        } catch (final Exception e) {
            throw e;
        }

        return result;
    }


    /**
     * コンストラクタ.
     */
    private Profile() {

    }
}
