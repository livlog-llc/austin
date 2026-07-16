# 設計概要

## 全体アーキテクチャ

AustinはRestletをServletとしてWARへ組み込む単一アプリケーションである。`AustinApplication`が4経路をルーティングし、`ServerResource`がHTTP処理、provider別ServiceがOAuth処理、Sql2o RepositoryがSQLiteの一時結果を扱う。独立したフロントエンドフレームワークはなく、`austin.js`と`close.html`を静的配信する。

```text
組込元画面 -> /app/austin.js -> OAuthResource -> 外部OAuth provider
                                         ^             |
                                         |             v
close.html <- CallbackResource <---------+------ callback
                    |
                    +-> Cookie/redirect query
                    +-> (Twitter OAuth 2.0) temporary SQLite -> ResultResource
```

## 主要コンポーネントと責務

| 層 | 実在コンポーネント | 責務 |
|---|---|---|
| 起動/route | `AustinApplication` | `/app`、`/oauth`、`/callback`、`/result`の割当 |
| Controller相当 | `OAuthResource` | 設定読込、開始要求の判定、Service選択、認可先へredirect |
| Controller相当 | `CallbackResource` | callback処理、結果のCookie/クエリ化、戻り先redirect |
| Controller相当 | `ResultResource` | SQLiteから一時結果を取得し、取得後削除してJSON化 |
| 共通Resource | `AbsBaseResource` | 設定キャッシュ、server mode判定、ドメイン判定、Cookie生成 |
| Service | `TwitterService`等8クラス | provider設定選択、認可URL、トークン交換、ユーザーID取得 |
| Repository | `Sql2oConfig`、`TemporaryRepository` | SQLite接続と`temporary` CRUD |
| Entity/Model | `TemporaryModel` | `KEY`、`VALUE`の行表現 |
| DTO | `Setting`、`Provider`、`Result`、helper DTO | 設定、OAuth結果、外部応答の表現 |
| View/Component相当 | `austin.js`、`close.html`、`example/index.html` | popup起動、結果受信/取得、デモ |

Spring MVCのController/View、JPA Entityは存在しない。Repository基底の`update`は存在するが、`TemporaryRepository.update`は存在しない`region_resources`を更新しており、現行フローからは呼ばれていない。

## データの流れ

### ブラウザフロー

`austin.js`がクライアント側UUIDを生成してOAuth開始を開く。セッションにはprovider実装によりOAuth service、request token、state、PKCE、key、return_url等を保持する。callback成功時は結果を60秒Cookieとredirect queryへ設定する。既定の`close.html`はCookieを読み、親へ`postMessage`後にCookieを削除してpopupを閉じる。TrelloだけはURL fragmentのtokenも読む。

### サーバー側結果フロー

README上は`implementation=server`と`key={uuid}`で開始し、`/result/{uuid}`で取得する。コード上のDB保存はTwitter OAuth 2.0 callbackのみ。保存値は`Result`のJSON文字列で、結果APIはそれを`TemporaryModel.value`内の文字列として返す。

## 外部連携

| Provider | 主な実装/接続先 | 結果 |
|---|---|---|
| Twitter | ScribeJava OAuth 1.0a / Twitter OAuth 2.0 | OAuth1はtoken由来ID、token/secret。OAuth2はtokenとraw responseのBase64 |
| Facebook | ScribeJava + RestFB | user ID、access token |
| LINE | LINE authorize/token/profile API | user ID、access token |
| Trello | `trello.com/1/authorize` | ServiceのResultは空。画面側でfragment tokenを取得 |
| Slack | ScribeJava Slack API | access token、raw responseのBase64。IDなし |
| Discord | ScribeJava + Discord `/api/v9/users/@me` | user ID、access token |
| Google | ScribeJava + Google userinfo v3 | `sub`、access token |
| GitHub | ScribeJava + `api.github.com/user` | user ID、access token |

## エラー処理

- Resource外周は例外をERRORログに記録して再throwする。
- callback内のprovider処理例外は握り、ブラウザ時は`status=ng`と`authentication_failure`を返す。HTTP statusは明示変更しない。
- 未許可ドメイン用例外には`@Status(403)`がある。
- 結果なしは例外ではなく`status=ng`。
- providerごとの外部HTTP status確認やエラー本文の扱いは不統一。

## ログ方針（現状）

INFO以上を日次ファイルと標準出力へ出す。providerによって認可URL、raw token response、外部URLをINFO出力している。認可URLやraw responseが資格情報・code・tokenを含み得るため、本番ログでの秘匿化が未実装である。例外はstack trace付き。相関IDや構造化ログはない。なお`logback.xml`はSpring Bootのdefaultsをincludeするが、POMにSpring Boot依存はないため、実行時解決を要確認。

## 既存設計上の注意点

- callback URLは受信したrequest URL中の`oauth`を`callback`へ文字置換して生成する。reverse proxy時のscheme/host補正はない。
- Serviceはsingletonだが要求状態は主にHTTP sessionへ置く。セッション固定、失効、並行popupの上書きを考慮する必要がある。
- Cookieに`Secure`、`HttpOnly`、`SameSite`指定がなく、access tokenを格納する。
- callback redirect queryにもtokenを含めるため、履歴・proxy・参照元ログへの露出リスクがある。
- CORSはワイルドカード。ドメイン検査ロジックの入力は要検証。
- `return_url`のallowlist検証が実装とREADMEで不一致。
- PKCE verifier/challengeがTwitter OAuth 2.0で固定文字列`challenge`。
- SQLiteがWAR展開先にあり、再デプロイ、複数instance、read-only展開での動作に制約がある。

## 変更時に守るべき設計方針

- route、provider名、Resultフィールド、設定JSONキーを互換性のある公開契約として扱う。
- provider固有処理は対応Serviceへ閉じ、Resourceはrouting/transport責務に保つ。
- callback state、session属性、結果伝達経路を変更する場合は全providerとpopup/server両フローを確認する。
- token、secret、認可code、個人情報をログ・テストfixture・文書・Gitへ含めない。
- DB変更はDDL、同梱SQLite、Repository、rollback手順を一体で設計し、現状の直接差し替えを暗黙に行わない。
- セキュリティ差分（return URL、CORS、Cookie、PKCE、server mode）は互換性と運用影響を人間に確認してから変更する。

