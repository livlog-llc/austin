/*
 * タイトル：ProtoPediaリニューアルプロジェクト.
 * 説明    ：
 * 著作権  ：Copyright(c) 2020 MA
 * 会社名  ：一般社団法人MA
 * 変更履歴：2020.06.02
 *         ：新規登録
 */
package jp.livlog.austin.share;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * URLのパラメータ設定をする.
 *
 * @author H.Aoshima
 * @version 1.0
 */
public class Parameters implements Serializable {

    /**
     * パラメータを保持する.
     *
     * @author H.Aoshima
     * @version 1.0
     */
    class Parameter implements Serializable {

        /**
         * シリアルバージョンUID.
         */
        private static final long serialVersionUID = 1L;

        /** name. */
        private String            name;

        /** value. */
        private String            value;

        /**
         * コンストラクタ.
         * @param pName String
         * @param pValue String
         */
        Parameter(final String pName, final String pValue) {

            this.name = pName;
            this.value = pValue;
        }


        /**
         * @return name
         */
        public String getName() {

            return this.name;
        }


        /**
         * @return value
         */
        public String getValue() {

            return this.value;
        }


        /**
         * @param pName セットする name
         */
        public void setName(final String pName) {

            this.name = pName;
        }


        /**
         * @param pValue セットする value
         */
        public void setValue(final String pValue) {

            this.value = pValue;
        }

    }

    /**
     * シリアルバージョンUID.
     */
    private static final long serialVersionUID = 1L;

    /** parameters. */
    private List <Parameter>  parameters       = new ArrayList <>();

    /**
     * @param name String
     * @param value Object
     */
    public final void addParameter(final String name, final Object value) {

        this.parameters.add(new Parameter(name, String.valueOf(value)));
    }


    /**
     * @return parameterList
     */
    public final List <Parameter> getParameters() {

        return this.parameters;
    }


    /**
     * @param pParameters セットする parameters
     */
    public final void setParameters(final List <Parameter> pParameters) {

        this.parameters = pParameters;
    }


    /*
     * (非 Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public final String toString() {

        return this.toQueryString(true);
    }


    /**
     * @param questF 先頭に?を付与する
     * @return String
     */
    public final String toQueryString(boolean questF) {

        final var sb = new StringBuffer();

        var first = true;
        for (final Parameter param : this.parameters) {
            if (first) {
                if (questF) {
                    sb.append("?");
                }
                sb.append(param.getName() + "=" + param.getValue());
                first = false;
            } else {
                sb.append("&" + param.getName() + "=" + param.getValue());
            }
        }
        return sb.toString();
    }
}
