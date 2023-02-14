package jp.livlog.austin.repositories;

import org.sql2o.Connection;

/**
 * リポジトリインターフェイス.
 *
 * @author H.Aoshima
 * @version 1.0
 *
 * @param <T>
 */
public abstract class Repository <T> {

    /** Connection. */
    private Connection connection;

    /**
     * コンストラクタ.
     * @param pConnection Connection
     */
    public Repository(final Connection pConnection) {

        this.connection = pConnection;
    }


    /**
     * 削除.
     * @param data <T>
     * @param args Objects
     * @return 件数
     */
    abstract int delete(T data, Object... args);


    /**
     * @return connection
     */
    public Connection getConnection() {

        return this.connection;
    }


    /**
     * 登録.
     * @param data <T>
     * @param args Objects
     * @return 件数
     */
    abstract int insert(T data, Object... args);


    /**
     * @param pConnection セットする Connection
     */
    public void setConnection(final Connection pConnection) {

        this.connection = pConnection;
    }


    /**
     * 更新.
     * @param data <T>
     * @param args Objects
     * @return 件数
     */
    abstract int update(T data, Object... args);
}
