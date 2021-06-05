package jp.livlog.austin.share;

import javax.servlet.http.HttpServletRequest;

import jp.livlog.austin.data.Result;
import jp.livlog.austin.data.Setting;

/**
 * ベース用サービスのインターフェイス.
 *
 * @author H.Aoshima
 * @version 1.0
 *
 */
public interface InfBaseService {

    /**
     * 認証処理.
     *
     * @param setting 設定
     * @param appKey アプリキー
     * @param request HttpServletRequest
     * @return 認証URL
     * @throws Exception 例外
     */
    String auth(Setting setting, String appKey, HttpServletRequest request) throws Exception;


    /**
     * コールバックURL取得.
     *
     * @param appKey アプリキー
     * @param request HttpServletRequest
     * @return コールバックURL
     */
    String getCallback(String appKey, HttpServletRequest request);


    /**
     * コールバック処理.
     *
     * @param setting 設定
     * @param appKey アプリキー
     * @return 結果
     * @throws Exception 例外
     */
    Result callback(Setting setting, String appKey, HttpServletRequest request) throws Exception;
}
