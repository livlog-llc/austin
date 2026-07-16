# 画面・クライアント仕様

## 画面一覧

Austinは画面アプリケーションではなく、2つの静的assetとリポジトリ内のデモ画面を持つ。

| URL/route | 種別 | 目的 | 関連ファイル |
|---|---|---|---|
| `/austin/app/austin.js` | JavaScript library | 組込元画面からpopup OAuthを開始し結果を受ける | `src/main/webapp/app/austin.js` |
| `/austin/app/close.html` | callback完了画面 | 結果を親windowへ通知しpopupを閉じる | `src/main/webapp/app/close.html` |
| 直接配信routeなし | 開発例 | localhost上でpopup/getを試す | `example/index.html` |
| 外部provider認可画面 | 外部画面 | 同意、ログイン、拒否 | Austin管理外 |

## 組込元画面（`austin.js`利用）

### 目的・表示項目

Austin自身はUIを描画しない。組込元がボタン、結果、エラーを実装する。`example/index.html`には`Open Popup`と`Get Data`の2ボタンだけがあり、結果はconsoleへ出す。

### 入力・操作

- constructor: Austinのorigin相当URLと`appKey`。
- `popup(provider, action)`: client UUIDを生成し、`/austin/oauth/{provider}/{appKey}?key={uuid}`をpopupで開く。
- `get()`: 最後に生成したUUIDで`/austin/result/{uuid}`をfetchし、JSONを返す。
- message listenerは`event.origin === Austin.austinURL`の場合だけ`event.data`をJSON parseしてcallbackへ渡す。

### 状態別表示

| 状態 | 現行挙動 | 組込元に必要な実装 |
|---|---|---|
| 通常 | popupを開き、成功結果をcallback | 成功表示、tokenの安全な受渡し |
| ローディング | 組込assetに表示なし | ボタン無効化、進行表示、timeout |
| データなし | `get()`結果が`status=ng` | 未完了/期限切れ/取得済みを区別する方針 |
| エラー | `status=ng`またはfetch/JSON例外 | 利用者向け表示、再試行、詳細を秘匿 |
| 権限なし | 専用表示なし。403/認可拒否になり得る | provider拒否とAustin domain拒否の区別 |
| popup不可 | `window.open`の戻り値を確認しない | popup blocker案内 |

### 使用API

- `GET /austin/oauth/{provider}/{app_key}?key={uuid}`
- `GET /austin/result/{uuid}`
- window `message` event

### 注意点

`Austin.austinURL`、`appKey`、`uuid`はstatic propertyのため、同一ページで複数instance/並行popupを使うと状態が共有・上書きされる。message listenerもpopupごとに追加され、解除されない。

## 完了画面（`close.html`）

### 目的・表示項目

callbackが設定した`austin-*` Cookieを読みResultを作り、全Cookie名に対して期限切れCookieをpath `/`で設定する。Trelloの場合はlocation hashの`token`を`oauthToken`へ設定する。親windowがあればJSON文字列を`postMessage`し、windowを閉じる。親がない場合は完了案内2段落を表示する。

### 入力

- Cookie: `austin-provider`,`austin-id`,`austin-oauth-token`,`austin-oauth-token-secret`,`austin-status`,`austin-other`,`austin-error-message`。
- Trello: `#token=...`。

### 操作・状態

- 利用者が操作するcontrolはない。
- 通常: 自動通知・自動close。
- ローディング: 表示なし。
- データなし: 空objectを通知する可能性がある。
- エラー: `status=ng`,`errorMessage=authentication_failure`を通知。parse/postMessage例外の画面処理なし。
- 権限なし: 専用表示なし。
- `window.opener`なし: 日本語の完了メッセージを表示。ただしHTMLの終了タグに崩れがあり、render確認が必要。

### セキュリティ上の境界

- `close.html`は送信先を`"*"`とするが、親側`austin.js`は受信originをAustin URLと比較する。
- JavaScriptがtoken Cookieを読む設計のため`HttpOnly`にはできない現行契約である。
- 全document Cookieを走査し、Austin以外のCookieにも削除用Set-Cookieを試みる。
- tokenがCookie、fragment、callback queryを経由し得る。

## デモ画面（`example/index.html`）

- localhost:8080のAustinを読み、固定app key文字列とGoogle providerを使用する例。
- jQuery 3.5.1をGoogle CDNから読み込む。
- `get()`応答の`data.result.value`を再度JSON parseし、`other`をBase64 decodeする例だが、GoogleのResultは通常`other`未設定であり、現行provider選択と処理例が整合しない可能性がある。
- デモ内app keyが実環境で有効か、公開してよい値かは未確認。app keyはsecret認証としては扱われないが、sample placeholderへ置換する判断が必要。

## 手動確認事項

- Chrome/Firefox/Safariでpopup、third-party/SameSite Cookie、message origin、popup close。
- 認可拒否、session切れ、popup blocker、複数同時認証、二度目の`get()`。
- Trello fragment処理、親なし完了画面、壊れたHTMLの見え方。
- `return_url`利用時は`close.html`を経由しないため、組込側がquery結果をどう安全に処理するか。

