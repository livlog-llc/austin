# テスト計画

## 現状

`src/test/java`は空で、POMにJUnit、Mockito等のテスト依存はない。`mvn test`はコンパイル確認として成功するが自動テスト件数は0。GitHub ActionsはJekyll Pagesのbuild/deployだけで、Javaのtest/packageを実行しない。fixture、mock、専用テストDBは存在しない。

## 実行コマンド

| 目的 | コマンド | 現状 |
|---|---|---|
| コンパイル兼テストphase | `mvn test` | 成功、テスト0件 |
| WAR生成 | `mvn package` | 実行対象。`target/austin.war`生成を確認する |
| clean build | `mvn clean install` | README記載。ローカル成果物を消すため必要時のみ実行 |
| DB schema確認 | `src/main/webapp/WEB-INF/sqlite3.exe src/main/webapp/WEB-INF/austin.sqlite3 ".schema"` | `temporary(KEY, VALUE, PRIMARY KEY(KEY))`を確認 |

lint、formatter、coverage、静的解析の設定はない。Maven compile phaseではversions pluginが更新候補を照会するため、ネットワーク状況に左右され得る。

## 推奨テスト種別

- Unit: provider選択、callback URL、domain判定、server mode、Result変換、TemporaryRepository。
- Integration: Restlet Resource + Servlet session +一時SQLiteでHTTP status、redirect、Cookie、JSON、delete-after-read。
- Contract: 8 providerの認可URLパラメータ、callback入力、外部API応答mock。
- E2E（手動/隔離環境）: 実providerのテストアプリでpopupとserver flow。
- Security: CSRF、open redirect、CORS、Cookie属性、token漏えい、UUID推測/再利用、ログ秘匿。
- Packaging: WAR内容、`setting.json`の供給方法、Tomcat起動、DB書込権限。

## 主要機能ごとの観点

| 機能 | 正常系 | 異常系・境界値 |
|---|---|---|
| OAuth開始 | 8 provider×有効app_keyが正しい認可先へredirect | 未知/空provider、未登録app_key、設定欠落、外部scope空、URL encoding |
| domain判定 | 許可hostのみ通過 | subdomain、port、case、Originなし、Refererなし、偽装host、malformed URI |
| callback | code/token交換、ユーザーID取得、session消去 | 拒否、code欠落、state欠落/不一致、session切れ、外部4xx/5xx/不正JSON |
| popup結果 | 親origin検査、CookieからResult復元、popup close | Cookie欠落/期限切れ、特殊文字、巨大token、親なし、Trello fragmentなし |
| return URL | 許可先へのredirect | 外部host、javascript等不正scheme、CRLF、空、相対URL。現実装との差分を検出 |
| result API | 登録結果を一度返し削除 | UUIDなし/未知、二度取得、同時取得、重複key、DB lock |
| packaging | WARをTomcatへdeployしroute/static asset/DBが利用可能 | read-only展開、再deploy、context path変更、proxy配下 |

## DB・外部API・認証・権限

- Repositoryは一時DBコピーを使い、insert/find/deleteとトランザクション・lock・重複主キーを検証する。本番同梱DBをテストで変更しない。
- 外部HTTPはmock serverまたはadapter化後のmockを使い、実tokenをfixtureにしない。成功、拒否、timeout、rate limit、invalid JSONを網羅する。
- 全OAuth 2.0でstate生成・一致・一度限りを確認し、OAuth 1.0aはrequest token/session結合を確認する。
- Austin固有の認可層がない事実を確認し、server modeやresult UUIDが実質的なアクセス制御になっていないことをセキュリティ試験に含める。

## 手動確認

- 各providerの開発者コンソールに登録したredirect URI、scope、API version。
- Tomcat 10+での起動、`web.xml` Servlet 4.0宣言との互換性。
- reverse proxy/TLS配下で生成callback URLが外部到達URLと一致すること。
- popup blocker、主要ブラウザのSameSite/Cookie制約、`postMessage` origin。
- logback設定のロードと日次ローテーション、秘密情報が出ないこと。
- GitHub Pages workflowが本リポジトリで本当に必要か。

## テスト不足箇所

全実装が未自動テストである。優先順位は、(1) `return_url`とdomain検査、(2) token伝達/ログ、(3) callback state、(4) resultの一度限り取得、(5) provider contract、(6) WAR/Tomcat smoke test。CIにも`mvn test`と`mvn package`を追加する判断が必要だが、本書作成ではCIを変更していない。

