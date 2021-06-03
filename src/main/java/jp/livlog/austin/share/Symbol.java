package jp.livlog.austin.share;

/**
 * 定数クラス.
 *
 * @author H.Aoshima
 * @version 1.0
 */
public final class Symbol {

    /**
     * コンストラクタ.
     */
    private Symbol() {

    }

    /***************************** 文字列定数定義 ************************************/
    /**
     * 空文字.
     */
    public static final String EMPTY           = "";

    /**
     * 半角スペース.
     */
    public static final String HANKAKU         = " ";

    /**
     * 全角スペース.
     */
    public static final String ZENKAKU         = "　";

    /**
     * アスタリスク.
     */
    public static final String ASTERISK        = "*";

    /**
     * ハイフン.
     */
    public static final String HYPHEN          = "-";

    /**
     * カンマ.
     */
    public static final String COMMA           = ",";

    /**
     * ドット.
     */
    public static final String DOT             = ".";

    /**
     * スラッシュ.
     */
    public static final String SLASH           = "/";

    /**
     * コロン.
     */
    public static final String COLON           = ":";

    /**
     * 特別な区切り.
     */
    public static final String SPECIAL_SEP     = "<--->";

    /**
     * シングル・ダブルクォート.
     */
    public static final String QUARTO          = "\"'";

    /**
     * 同上.
     */
    public static final String DITTO           = "↑";

    /**
     * マイナス.
     */
    public static final String MINUS           = "−";

    /**
     * 改行.
     */
    public static final String SEP             = System.getProperty("line.separator");

    /**
     * タブ.
     */
    public static final String TAB             = "\t";

    /***************************** システム定数定義 ************************************/
    /**
     * ServletContext.
     */
    public static final String SERVLET_CONTEXT = "org.restlet.ext.servlet.ServletContext";

    /**
     * オースティン設定.
     */
    public static final String AUSTIN_SETTING  = "jp.livlog.austin.Setting";

    /**
     * 設定パス.
     */
    public static final String SETTING_PATH    = "/setting.json";
}
