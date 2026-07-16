# 要件整理

## 文書の位置づけ

本書は2026-07-16時点のリポジトリに存在する実装、設定、READMEから復元した既存仕様である。将来仕様や外部公開可否を確定するものではない。実装とREADMEが異なる場合は現行実装を事実として記し、差分を明示する。

## システムの目的と対象ユーザー

AustinはJava 17/Tomcat上で動作し、複数のOAuthプロバイダーをブラウザまたはサーバーから共通の入口で利用する認証統合ミドルウェアである。対象はAustinを組み込むWebアプリケーション開発者と、そのアプリケーションのOAuth利用者である。Austin自身は利用者アカウント、業務権限、恒久プロフィールを管理しない。

## 主な利用シナリオ

1. クライアントは`Austin(austinURL, appKey)`を生成し、`popup(provider, callback)`を呼ぶ。
2. Austinは`GET /oauth/{provider}/{app_key}`から外部プロバイダーへリダイレクトする。
3. プロバイダーが`GET /callback/{provider}/{app_key}`へ戻し、Austinがトークンと必要なユーザーIDを取得する。
4. ブラウザフローでは短命Cookieを`app/close.html`が読み、`postMessage`で親画面へ結果を返す。
5. 一部のサーバー側フローではUUIDをキーとしてSQLiteへ結果を一時保存し、`GET /result/{uuid}`が一度だけ返して削除する。

## 機能要件（確認済み）

- 対応識別子は`twitter`、`facebook`、`line`、`trello`、`slack`、`discord`、`google`、`github`。
- `setting.json`の`app_key`と`provider_name`の組み合わせでOAuthクライアント設定を選ぶ。
- OAuth開始、コールバック、結果取得、`/app`配下の静的配信を提供する。
- `implementation=server`指定時は開始・結果取得でドメイン検査を迂回する。
- `return_url`指定時はコールバック後にそのURLへリダイレクトする。
- 結果は`status`、`provider`、`id`、`oauthToken`、`oauthTokenSecret`、`other`を取り得る。値の有無はプロバイダー依存である。
- Cookieの有効期間は60秒、パスは`/`。
- `/result/{uuid}`で見つかった一時結果は応答時に削除される。

## 非機能要件として確認できた事項

- Java 17、Maven 3.6.3以上、WAR、Tomcat/Jakarta Servletを前提とする。
- ログは標準出力と`${catalina.base}/logs/austin.YYYY-MM-DD.log`へ出力し、日次・30日保持を設定している。
- 設定はクラスパス`/setting.json`から読み、ServletContextへキャッシュする。動的再読込機構はない。
- DBは展開済みWebアプリの`WEB-INF/austin.sqlite3`を直接利用する。
- 可用性、性能目標、監視、SLA、アクセシビリティ、対応ブラウザは定義されていない。

## 入力と出力

| 区分 | 入力 | 出力 |
|---|---|---|
| OAuth開始 | pathの`provider`、`app_key`、任意の`key`、`implementation`、`return_url` | 外部認可URLへの303相当リダイレクト |
| コールバック | プロバイダー固有の`code`、`state`、`oauth_verifier`等 | `close.html`または`return_url`へのリダイレクト。結果をCookie/クエリに格納 |
| 結果取得 | pathの`uuid`、任意の`implementation` | JSONの`status`と、存在時は`result:{key,value}` |
| 設定 | `domains`とprovider設定 | OAuthサービス選択・認可URL構築 |

## 認証・権限

- Austin API自体へのログインやロールベース権限はない。`app_key`は設定選択キーであり、秘密として検証される認証資格情報ではない。
- ブラウザ開始・結果取得には許可ドメイン判定を意図した処理があるが、現在の渡し値はHTTP `Origin`/`Referer`ヘッダーではなくRestlet参照情報であり、期待どおりの制限になるか要検証。
- CORS応答は`Access-Control-Allow-Origin: *`。
- CSRF `state`確認はLINE、Slack、Twitter OAuth 2.0で確認した。その他のOAuth 2.0サービスでは統一されていない。

## 正常系・異常系・境界値

- 正常系: 有効なprovider/app_key、プロバイダー認可成功、ユーザー情報取得成功、結果の一度限り取得。
- 異常系: 未登録provider/app_key、認可拒否、code/state欠落、state不一致、外部API失敗、設定欠落、DBアクセス失敗、未許可ドメイン、結果未登録。
- 境界値: 空または未知のprovider/app_key/key、同一UUIDの重複登録、結果の二度目取得、60秒経過後のCookie、nullの結果フィールド、長い`other`/トークン、壊れた`return_url`。
- 実装上、未知のproviderは`Objects.requireNonNull`由来の例外、結果なしはHTTP成功内の`{"status":"ng"}`となる。統一エラー形式はない。

## 既存文書・実装の差分

- READMEは`return_url`のホストが`domains`に含まれる必要があるとするが、現行`OAuthResource`は値をセッションへ保存するだけで許可リスト検証を行わない。
- READMEはサーバー側結果取得を一般機能として説明するが、SQLiteへの明示的保存はTwitter OAuth 2.0内でのみ確認できた。
- READMEはTomcat 10+/Servlet 6.0系とする一方、`web.xml`はServlet 4.0名前空間・スキーマを宣言する。
- READMEの対応プロバイダー一覧は列挙型と一致するが、Trelloコールバックは空の`Result`を返し、トークンは`close.html`がURLフラグメントから補うブラウザ依存実装である。
- GitHub Actionsはアプリのビルド/テストではなくJekyll GitHub Pagesのみを実行する。

## 未確認事項・要確認事項

- 各プロバイダーの現行OAuth仕様・登録済みcallback URLとの互換性、実環境でのE2E成功可否。
- `return_url`を外部入力として許す正式要件と、オープンリダイレクト対策。
- トークンをCookie、URLクエリ、JavaScriptへ返すことのセキュリティ承認。
- `implementation=server`を誰が使用できるか、UUIDの生成・推測耐性・有効期限。
- サーバー側結果保存を全providerに広げる意図の有無。
- 本番性能、同時実行、監視、バックアップ、障害復旧、データ保持要件。

