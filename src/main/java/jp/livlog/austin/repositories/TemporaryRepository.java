package jp.livlog.austin.repositories;

import org.sql2o.Connection;

import jp.livlog.austin.model.TemporaryModel;

/**
 * 一時保存リポジトリ.
 *
 * @author H.Aoshima
 * @version 1.0
 *
 */
public class TemporaryRepository extends Repository <TemporaryModel> {

    /**
     * コンストラクタ.
     * @param pConnection Connection
     */
    public TemporaryRepository(final Connection pConnection) {

        super(pConnection);
    }


    /**
     * @param regionResourcesId String
     * @return RegionResourcesModel
     */
    public TemporaryModel findById(final String key) {

        // SQL
        final var queryText = new StringBuilder();
        queryText.append("SELECT ");
        queryText.append("   * ");
        queryText.append("FROM temporary ");
        queryText.append("WHERE KEY=:key ");

        final var query = this.getConnection().createQuery(queryText.toString())
                .setAutoDeriveColumnNames(true)
                .addParameter("key", key);

        // 実行
        final var model = query.executeAndFetchFirst(TemporaryModel.class);

        return model;
    }


    @Override
    public int delete(final TemporaryModel data, final Object... args) {

        final var queryText = new StringBuilder();

        queryText.append("DELETE FROM temporary ");
        queryText.append(" WHERE KEY=:key ");

        // execute query
        final var effectiveCount = this.getConnection().createQuery(queryText.toString()).bind(data)
                .executeUpdate().getResult();

        return effectiveCount;
    }


    @Override
    public int insert(final TemporaryModel data, final Object... args) {

        final var queryText = new StringBuilder();
        queryText.append("INSERT INTO ");
        queryText.append("temporary ( ");
        queryText.append("    KEY, ");
        queryText.append("    VALUE ");
        queryText.append(") VALUES ( ");
        queryText.append("    :key , ");
        queryText.append("    :value ");
        queryText.append(") ");

        // execute query
        final var effectiveCount = this.getConnection().createQuery(queryText.toString()).bind(data)
                .executeUpdate().getResult();

        return effectiveCount;
    }


    @Override
    public int update(final TemporaryModel data, final Object... args) {

        final var queryText = new StringBuilder();

        queryText.append("UPDATE region_resources ");
        queryText.append("   SET VALUE=:value ");
        queryText.append(" WHERE KEY=:key ");

        // execute query
        final var effectiveCount = this.getConnection().createQuery(queryText.toString()).bind(data)
                .executeUpdate().getResult();

        return effectiveCount;
    }

}
