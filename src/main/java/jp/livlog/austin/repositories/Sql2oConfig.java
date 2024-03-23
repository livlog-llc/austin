package jp.livlog.austin.repositories;

import javax.servlet.ServletContext;

import org.apache.commons.dbcp.BasicDataSource;
import org.sql2o.Sql2o;

import lombok.Getter;

/**
 * Sql2oConfigクラス.
 *
 * @author H.Aoshima
 * @version 1.0
 */
@Getter
public class Sql2oConfig {

    /** データソース. */
    private final BasicDataSource dataSource;

    /** Sql2o. */
    private final Sql2o           sql2o;

    /**
     * コンストラクタ.
     */
    public Sql2oConfig(final ServletContext servletContext) {

        final var dbname = servletContext.getRealPath("/WEB-INF/austin.sqlite3");

        this.dataSource = new BasicDataSource();
        this.dataSource.setDriverClassName("org.sqlite.JDBC");
        this.dataSource.setUrl("jdbc:sqlite:" + dbname);
        this.dataSource.setUsername("root");
        this.dataSource.setPassword("root");
        this.sql2o = new Sql2o(this.dataSource);
    }

}
