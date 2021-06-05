package jp.livlog.austin.data;

import lombok.Data;

/**
 * 結果データ.
 *
 * @author H.Aoshima
 * @version 1.0
 *
 */
@Data
public class Result {

    String id;

    String oauthToken;

    String oauthTokenSecret;

}
