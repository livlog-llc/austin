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

        final ProviderType[] types = ProviderType.values();
        for (final ProviderType type : types) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }

}
