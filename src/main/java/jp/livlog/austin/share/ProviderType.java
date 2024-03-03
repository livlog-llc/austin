package jp.livlog.austin.share;

/**
 * プロバイダの列挙型.
 *
 * @author H.Aoshima
 * @version 1.0
 */
public enum ProviderType {

    /** Twitter. */
    TWITTER(1, "twitter"),
    /** Facebook. */
    FACEBOOK(2, "facebook"),
    /** Line. */
    LINE(3, "line"),
    /** Trello. */
    TRELLO(4, "trello"),
    /** Slack. */
    SLACK(5, "slack"),
    /** Discord. */
    DISCORD(6, "discord"),
    /** Google. */
    GOOGLE(7, "google"),
    ;

    /** cd. */
    public int    cd;

    /** name. */
    public String name;

    /**
     * コンストラクタ.
     * @param pCd int
     * @param pName String
     */
    ProviderType(final int pCd, final String pName) {

        this.cd = pCd;
        this.name = pName;
    }


    /**
     * @param name String
     * @return ProviderType
     */
    public static ProviderType getType(final String name) {

        final var types = ProviderType.values();
        for (final ProviderType type : types) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }

}
