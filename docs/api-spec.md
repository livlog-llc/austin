# API仕様

## 前提

以下は`AustinApplication`、3 Resource、8 Service、`austin.js`から確認した現行契約である。WARの通常context pathが`/austin`であるため例は`/austin`を付けるが、route自体はアプリケーションルート相対である。全APIはGETで、request bodyはない。

## API一覧

| Method | Path | 概要 | 主な実装 |
|---|---|---|---|
| GET | `/oauth/{provider}/{app_key}` | OAuth認可を開始し、外部providerへredirect | `OAuthResource`、対象`*Service.auth` |
| GET | `/callback/{provider}/{app_key}` | provider callbackを処理し、結果返却画面/指定URLへredirect | `CallbackResource`、対象`*Service.callback` |
| GET | `/result/{uuid}` | 一時結果をJSONで一度だけ取得 | `ResultResource`、`TemporaryRepository` |
| GET | `/app/*` | JavaScript/HTML静的asset | Restlet `Directory` |

## GET `/oauth/{provider}/{app_key}`

### Request

| 位置 | 名前 | 必須 | 内容/検証 |
|---|---|---|---|
| path | `provider` | 必須 | `twitter`,`facebook`,`line`,`trello`,`slack`,`discord`,`google`,`github`の完全一致。未知値は例外 |
| path | `app_key` | 必須 | `setting.json`内でproviderとの組み合わせが一致する必要あり。未一致は例外 |
| query | `key` | 条件付き | client UUID。`austin.js`が生成。コード上でDB保存に使うのはTwitter OAuth 2.0 |
| query | `implementation` | 任意 | 大文字小文字を無視して`server`ならserver-side扱い |
| query | `return_url` | 任意 | sessionへ保存。現実装にscheme/host allowlist validationなし |

通常モードでは許可domain判定を試みる。`domains`が空なら拒否。現行コードはHTTP `Origin`/`Referer` headerを直接参照していないため、実効性は要確認。server modeでは検査しない。

### Response / status

- 成功: providerの認可URLへSee Other redirect（通常303）。空body。
- 未許可domain: `NotspecifiedDomainError`に`@Status(403)`。
- 未知provider、未登録app_key、設定/URL生成失敗: 例外をERROR log後に再throw。統一JSON error bodyなし。実際のHTTP statusはRestlet例外mappingに依存。
- `Access-Control-Allow-Origin: *`を設定。

## GET `/callback/{provider}/{app_key}`

### Request

pathは開始APIと同じ。queryはproviderが付与するため一律でない。

| Provider | 主なcallback入力 | state検証（コード確認） |
|---|---|---|
| Twitter OAuth 1.0a | `oauth_verifier` | OAuth1 request token/sessionを使用 |
| Twitter OAuth 2.0 | `code`,`state` | あり。sessionのstateと比較 |
| Facebook | `code` | 明示state比較なし |
| LINE | `code`,`state` | あり |
| Trello | tokenはredirect後のURL fragment | Service callbackでは処理なし |
| Slack | `code`,`state` | あり |
| Discord | `code` | 明示state比較なし |
| Google | `code` | 明示state比較なし |
| GitHub | `code` | 明示state比較なし |

`implementation=server`判定はcallback URLへproviderが当該queryを保持する場合だけ成立する。開始時の値をsessionへ保存する共通処理はない。

### Response

- 既定: `/app/close.html`へredirect。
- 開始時に`return_url`がsession保存されていれば、そのURLへredirect。
- 成功時redirect query: `austin-status=ok`に加え、値があるものだけ`austin-provider`,`austin-id`,`austin-oauth-token`,`austin-oauth-token-secret`,`austin-other`。
- 失敗時redirect query: `austin-status=ng`、provider処理例外時は`austin-error-message=authentication_failure`。
- 通常モードでは同名相当のCookieも設定する。Cookieは最大60秒、path `/`。`Secure`、`HttpOnly`、`SameSite`の明示なし。
- provider処理例外はcallback内で捕捉されredirect結果へ変換されるため、認証失敗でもHTTP error statusとは限らない。
- `Access-Control-Allow-Origin: *`。

### Result項目

| 項目 | 型 | 説明 |
|---|---|---|
| `status` | string | `ok`または`ng`（Cookie/query名は`austin-status`） |
| `provider` | string | provider識別子 |
| `id` | string/null | 外部user ID。Trello/Slack/Twitter OAuth2では未設定を取り得る |
| `oauthToken` | string/null | access token |
| `oauthTokenSecret` | string/null | 主にTwitter OAuth1 token secret |
| `other` | string/null | provider raw response等のBase64。形式は統一されない |
| `errorMessage` | string/null | 現状は`authentication_failure` |

## GET `/result/{uuid}`

### Request / validation

- path `uuid`: `temporary.KEY`の完全一致。形式、長さ、UUID妥当性のvalidationなし。
- query `implementation=server`: server modeとなりdomain検査を迂回。
- 通常モードはOAuth開始と同種のdomain検査を行う。
- 認証・所有者照合・有効期限検査はない。

### Response

登録あり:

```json
{
  "status": "ok",
  "result": {
    "key": "<uuid>",
    "value": "<ResultをJSON化した文字列>"
  }
}
```

登録なし、または取得済み:

```json
{"status":"ng"}
```

取得に成功した行は同じDB connection内で直後に削除される。ただし明示transaction制御はない。HTTP statusはいずれも通常成功。DB/設定例外はlog後に再throw。`Access-Control-Allow-Origin: *`。

## 静的asset

- `GET /app/austin.js`: global class `Austin`を提供。`popup`と`get`を持つ。
- `GET /app/close.html`: Cookie/fragmentから結果を組み立て、`window.opener.postMessage(..., "*")`後に閉じる。
- Directory listing可否、cache header、MIME設定はRestlet/Tomcat既定に依存し未確認。

## Validation・認証・エラーの共通注意

- request body validation frameworkはない。
- `app_key`はprovider設定検索キーで、API client認証ではない。
- `return_url`の現実装はREADME記載のdomain制限と矛盾する。
- callback queryへtokenを含めるため、外部指定`return_url`との組み合わせは特に要確認。
- status code、error body、provider error mappingは公開仕様として未確定。

