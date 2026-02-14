# Austin

[![Build Status](https://travis-ci.org/livlog-llc/austin.svg?branch=master)](https://travis-ci.org/livlog-llc/austin)  [![Open Source](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)](https://github.com/livlog-llc/austin) [![Java Version](https://img.shields.io/badge/java-17-blue.svg)](https://openjdk.java.net/projects/jdk/17/)

**Austin**は、Java 17 + Tomcat 環境で動作する OAuth 認証統合ミドルウェアです。

## 何ができるのか

フロントエンドから `austin.js` を呼び出すだけで、複数プロバイダーの OAuth 認証を共通のインターフェースで実行できます。

## 対応プロバイダー

現在の実装で利用できる `provider_name` は以下です。

- `twitter`
- `facebook`
- `line`
- `trello`
- `slack`
- `discord`
- `google`
- `github`

## インストール

### 必要条件

- Java 17
- Maven 3.6.3+
- Tomcat 10+（Jakarta Servlet API 6.0 系）

### 手順

1. **プロジェクトをクローン**

   ```sh
   git clone https://github.com/livlog-llc/austin.git
   cd austin
   ```

2. **ビルド**

   ```sh
   mvn clean install
   ```

3. **デプロイ**

   生成された `target/austin.war` を Tomcat に配置します。

## 設定ファイル (`setting.json`)

### 配置場所

`src/main/resources/setting.json`

> リポジトリにはテンプレートとして `src/main/resources/setting_sample.json` があるため、コピーして `setting.json` を作成してください。

### 構造

- `domains`: 許可するリクエスト元ドメイン（**ホスト名のみ**を推奨）
- `providers`: OAuth プロバイダーごとの設定

```json
{
  "domains": [
    "127.0.0.1",
    "example.com"
  ],
  "providers": [
    {
      "app_name": "MyApp",
      "app_key": "myapp123",
      "provider_name": "github",
      "client_id": "xxxx",
      "client_secret": "yyyy",
      "scope": "read:user user:email",
      "apiVersion": ""
    }
  ]
}
```

### 主な注意点

- `provider_name` は上記「対応プロバイダー」の文字列と完全一致させてください。
- `twitter` は `apiVersion` が `2.0` の場合に OAuth 2.0 フローで動作します。
- `facebook` は Graph API バージョン解決に `apiVersion` を使用します（例: `v20.0`）。

## 使い方（クライアントサイド）

```html
<script src="https://your-domain/austin/app/austin.js"></script>
<script>
const austin = new Austin("https://your-domain", "your-app-key");

austin.popup("github", function(data) {
  if (data.status === 'ok') {
    console.log(data.provider);         // github
    console.log(data.id);               // provider user id
    console.log(data.oauthToken);       // access token
    console.log(data.oauthTokenSecret); // OAuth1系のみ（未使用なら空）
    console.log(data.other);            // provider依存の追加情報（必要時）
  } else {
    console.log(data.errorMessage);
  }
});
</script>
```

## サーバサイドフロー

`implementation=server` を付与すると、サーバサイド利用モードで呼び出せます。

- 認証開始:
  - `https://your-domain/austin/oauth/{provider}/{app_key}?implementation=server&key={uuid}`
- 認証開始（戻り先指定）:
  - `https://your-domain/austin/oauth/{provider}/{app_key}?implementation=server&key={uuid}&return_url={urlencoded_return_url}`
- 結果取得:
  - `https://your-domain/austin/result/{uuid}?implementation=server`

`return_url` のホストは `setting.json` の `domains` に含まれている必要があります。

## ライセンス

MIT License
