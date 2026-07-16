# AI開発指示

## 役割分担

リポジトリ直下の`AGENTS.md`は、どのAIコーディングエージェントにも最初に読ませる短い共通契約である。本書はAustin固有の実装根拠、調査順序、セキュリティ境界、変更判断を詳述する。矛盾時は人間の依頼、`AGENTS.md`、本書の順に確認し、推測で解消しない。

## 実装前に読むファイル

1. `AGENTS.md`、`README.md`、本書、`docs/requirements.md`、`docs/design.md`。
2. HTTP変更は`AustinApplication`、対象`resource/*`、`AbsBaseResource`、`docs/api-spec.md`。
3. OAuth変更は対象`service/*Service.java`、`InfBaseService`、`ProviderType`、`Provider`、`Result`。
4. DB変更は`sql/create_temporary.sql`、`TemporaryModel`、`TemporaryRepository`、`Sql2oConfig`、`docs/db-operations-rollback.md`。
5. 画面変更は`austin.js`、`close.html`、`example/index.html`、`docs/screen-spec.md`。
6. build/deploy変更は`pom.xml`、`web.xml`、`logback.xml`、`.github/workflows/*`。

## 変更してよい範囲

- 依頼された範囲のJava、静的asset、テスト、文書。
- 必要最小限の設定変更。ただし依存、CI、DB、公開契約は依頼または人間承認がある場合のみ。
- 既存のprovider別Service分離、Restlet Resource、DTO命名に合わせる。

## 変更してはいけない範囲・禁止事項

- 指示なしにprovider、route、JSON/Cookie/queryのフィールド名、設定キーを削除・変更しない。
- 指示なしにDB schema、同梱SQLite、依存version、CI、デプロイ方式を変更しない。
- 実在しないコマンド、endpoint、テーブル、運用手順を作らない。
- 未確認の外部API仕様を記憶だけで更新しない。公式資料と実環境制約を確認する。
- `setting.json`、token、client secret、code、個人情報を読み上げ、貼付、ログ追加、fixture化、commitしない。
- 本番DBや実provider資格情報を使って自動テストしない。
- エラーを握りつぶす、秘密値を例外へ含める、既存のセキュリティ検査を弱める変更をしない。

## 実装パターン

- Java 17、4空白、`final`/`var`を使う既存スタイル、package構成に合わせる。大規模整形を混ぜない。
- provider追加は`ProviderType`、Service、`AbsBaseResource`、両Resourceのswitch、sample設定、API/要件/テスト文書を一貫更新する。
- session属性の生成・検証・削除を対にし、並行要求とnullを考慮する。
- 外部応答はHTTP statusを検証し、秘密情報をログしない。
- DB queryはbind parameterを使用する。テーブル/column変更はmigrationとrollback設計を先に作る。
- 既存の矛盾を仕様として固定化せず、テストで現挙動を記録し人間判断を仰ぐ。

## 実装後の確認

- 最低限`mvn test`。WAR/Servlet/assetに触れた場合は`mvn package`。
- DB変更時は隔離DBでschemaとCRUDを検証し、同梱本体を直接試験しない。
- API/画面/設定/運用が変わる場合は対応する`docs/*.md`とREADMEの整合を確認する。
- `git diff --check`、`git status --short`、差分中の秘密情報パターンを確認する。秘密値そのものを検索結果へ出力しない方法を選ぶ。
- 自動テストがない領域は、実行していないことと手動確認手順を報告する。

## Secrets・個人情報

- `src/main/resources/setting.json`は`.gitignore`対象だが、ローカルに存在し得る。原則として内容を表示せず、必要ならキー名や存在だけを確認する。
- sampleには明確なplaceholderだけを置く。実値らしきものを見つけたら転記せず、漏えい範囲の確認とprovider側ローテーションを人間へ依頼する。
- tokenをCookie/URL/log/DBへ渡す既存挙動はセキュリティ上の境界であり、安易に拡大しない。

## 人間に確認すべき判断

- 公開APIの破壊的変更、provider廃止/追加、scopeやAPI version変更。
- `return_url` allowlist、CORS、Cookie属性、PKCE/state、server modeのアクセス制御。
- DB migration、バックアップ/RPO/RTO、データ保持、複数instance対応。
- 本番デプロイ、資格情報ローテーション、外部公開、ログ保持/監視。
- READMEと実装が矛盾する場合に、どちらを正とするか。

