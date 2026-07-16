# DB運用・ロールバック

## DB構成

SQLite JDBC + Sql2oを使用する。接続先はServletContextのreal pathから求めた`/WEB-INF/austin.sqlite3`で、JDBC URLは`jdbc:sqlite:<real-path>`。接続poolはCommons DBCP `BasicDataSource`で構成する。環境変数や外部DB接続設定はない。

実DBと`sql/create_temporary.sql`で確認できたschemaは1テーブルのみ。

```sql
CREATE TABLE temporary (
  KEY,
  VALUE,
  PRIMARY KEY(KEY)
);
```

## 主要テーブル

| テーブル | 目的 | 主キー | 外部キー | index | 重要column |
|---|---|---|---|---|---|
| `temporary` | OAuth結果の一時受渡し | `KEY` | なし | PK由来のみ | `KEY`: client指定識別子、`VALUE`: `Result` JSON文字列 |

SQLite DDL上は型、NOT NULL、長さ、default、作成/失効日時が定義されない。明示的な追加index、trigger、viewはない。

## Repository操作

- `insert`: bind parameterで`KEY`,`VALUE`を挿入。同一KEYはPK違反。
- `findById`: bind parameterで完全一致し先頭1件。
- `delete`: bindしたKEYで削除。
- `ResultResource`はfind後にdeleteするが、明示transactionや排他処理はない。
- `update`: `TemporaryRepository`に存在するが、誤って`region_resources`を対象とする。現行呼出箇所はなく、使用禁止として扱う。

現行コードで一時結果をDBへ書くことが確認できたのは`TwitterService.processOAuth20Callback`のみ。他providerのserver-side保存は確認できない。

## マイグレーション方式

Flyway/Liquibase等のmigration framework、version table、自動起動DDLは存在しない。SQLファイルと同梱SQLite binaryが手動管理されている。したがって現時点で安全な本番migration手順は確立していない。`sqldiff.exe`、`sqlite3.exe`、`sqlite3_analyzer.exe`が`WEB-INF`に同梱されるが、運用での使用手順や出所/versionは文書化されていない。

## データ更新時の注意点

- 本番作業前にアプリ停止または書込停止を行い、SQLite file lockと処理中OAuth callbackを避ける。正式停止手順は未確認。
- WAR再deployで展開先DBが上書き/消失する可能性をTomcat設定ごと確認する。
- `KEY`には形式validation、所有者、期限がない。手動挿入・更新を通常運用にしない。
- `VALUE`はJSON文字列の二重構造であり、DTO変更との後方互換を確認する。
- 複数Tomcat instanceではDBを共有しない設計になる可能性が高い。構成は未確認。
- tokenを含み得るため、DB file、backup、dumpを秘密情報としてアクセス制御・暗号化・廃棄する。

## バックアップ方針

リポジトリ内に本番バックアップ、保管期間、暗号化、復元試験、RPO/RTOの定義はない。暫定的にschema変更やbinary差し替え前には、アプリ書込を止め、SQLiteの整合したbackup機能または停止中file copyで時刻付き退避を取得する。保管先、暗号鍵、保持期間、担当者は本番責任者が決定する。単なる稼働中file copyを安全と仮定しない。

`temporary`は一時データだけのため、業務上は復元より再認証を選ぶ可能性があるが、これは未決定事項である。

## ロールバック手順（方式確立までの安全な骨子）

1. 変更前にdeploy版、DB path、schema、file size、利用中process、空き容量を確認する。秘密値をdump/logへ出さない。
2. 新規callbackを停止し、アプリがDBを使用していないことを確認する。
3. 整合した変更前backupとchecksumを取得し、別保管先で保護する。
4. 承認済みmigrationをcopy DBで予行し、schema/row count/代表CRUDとrollbackを確認する。
5. 本番変更後、`PRAGMA integrity_check`、schema、アプリ疎通、一時結果insert/read/deleteを秘密でないデータで確認する。
6. 失敗時はアプリを停止したまま変更後DBを証跡として退避し、変更前DBを元の絶対pathへ戻す。
7. 旧WARとのschema互換を確認して旧版をdeployし、疎通後に受付を再開する。

上記は確定した本番runbookではない。Tomcat停止/切替、backup保存先、承認者、連絡、監視は運用担当が補完する。

## 戻せない・戻しにくい変更

- column/table削除、型/意味変更後に旧値を保持しないmigration。
- token/個人情報の不可逆変換、暗号鍵喪失。
- 新版が書いたVALUEを旧DTOが読めない変更。
- backupなしのDB file置換、破損、WAR redeployによる消失。
- 外部provider側で発行/失効したtokenや認可状態はDB rollbackでは戻らない。

## 本番作業前の確認事項

- DBの実配置、所有者、permission、容量、Tomcat deploy方式、複数instance/volume構成。
- backup/restoreの責任者、保管先、暗号化、保持、RPO/RTO、復元試験結果。
- migration SQL、forward/rollback SQL、旧新WAR互換性、停止時間、監視/連絡計画。
- 処理中一時結果を破棄して利用者に再認証を求められるか。
- 同梱SQLite utilitiesのversion、信頼性、実行許可。本番では管理された公式toolを優先する判断。
- 実資格情報を含むローカル`setting.json`やDB backupをGit、artifact、ticket、chatへ添付しないこと。

