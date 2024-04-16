# Austin

[![Build Status](https://travis-ci.org/livlog-llc/austin.svg?branch=master)](https://travis-ci.org/livlog-llc/austin)  [![Open Source](https://badges.frapsoft.com/os/v1/open-source.svg?v=103)](https://github.com/livlog-llc/austin) [![Java Version](https://img.shields.io/badge/java-17-blue.svg)](https://openjdk.java.net/projects/jdk/17/)

**Austin**は、Java17を基盤とし、Tomcat Webサーバーで動作するオープンソースのOAuth認証統合ソリューションです。

## 何ができるのか？

このプロジェクトは、SNSプラットフォーム（Twitter、Facebook、LINEなど）のOAuth認証を簡単に統合し、Web開発者がAPI統合に費やす時間を削減することができます。

## 主な特徴

- **対応プラットフォーム**: 複数の主要なSNSに対応。
- **OAuth認証の簡素化**: 複雑な認証プロセスを抽象化し、簡単に統合。
- **オープンソース**: 誰でも利用・改善が可能。

## インストール

### 必要条件
- Java 17がインストールされていること
- Maven 3.6.3以上がインストールされていること

### インストール手順

1. **プロジェクトのクローン**
   ```sh
   git clone https://github.com/austin/austin.git
   cd austin
   ```

2. **Mavenを使用した依存関係の解決とビルド**
   ```sh
   mvn clean install
   ```

   このコマンドは、`pom.xml` に定義された依存関係を解決し、プロジェクトをビルドします。このプロセスは、必要なライブラリをダウンロードし、アプリケーションの実行可能なWARファイルを生成します。

3. **WARファイルのデプロイ**
   生成された `austin.war` ファイルをTomcatまたは任意のJava対応Webサーバーにデプロイします。

## `settings.json` 設定方法

### ファイルの配置
プロジェクトディレクトリの `src/main/resources` フォルダに `settings.json` ファイルを配置してください。このファイルは、アプリケーションが起動時に読み込む設定情報を含んでいます。

### 設定ファイルの編集
`settings.json` は以下のように構成されています。

- **domains**: 認証リクエストを許可するドメインのリスト。ローカル開発や実運用環境のドメインを指定します。
- **providers**: 各OAuthプロバイダー（SNSサービス等）の設定。各プロバイダーごとに設定を行います。

以下のテンプレートを基に、実際の値に置き換えて設定してください。

```json
{
  "domains": [
    "your-local-ip:port",
    "your-production-domain.com"
  ],
  "providers": [
    {
      "app_name": "実際のアプリケーション名",
      "app_key": "アプリケーション固有のキー",
      "provider_name": "プロバイダ名（例：twitter）",
      "client_id": "プロバイダから取得したクライアントID",
      "client_secret": "プロバイダから取得したクライアントシークレット",
      "scope": "リクエストする権限（例：email,user_posts）",
      "apiVersion": "使用するAPIのバージョン（例：v4.0）"
    }
    // 他のプロバイダーも同様に追加
  ]
}
```

### `app_key` の生成
`app_key` はセキュリティを強化するために、ランダムな値を生成して使用することが推奨されます。パスワード生成ツールを利用して、一意かつ安全なキーを生成してください。

以下のウェブサイトで生成することができます：[ランダムキー生成ツール](https://www.luft.co.jp/cgi/randam.php)

### 設定例と参考資料
`src/main/resources` ディレクトリには `setting_sample.json` というサンプル設定ファイルも配置されています。このファイルを参考にして、具体的な設定値を理解し、自身のプロジェクトに合わせてカスタマイズしてください。

以下は具体的な設定の例です。

```json
{
  "domains": [
    "127.0.0.1:5500",
    "example.com"
  ],
  "providers": [
    {
      "app_name": "MyApp",
      "app_key": "myapp123",
      "provider_name": "twitter",
      "client_id": "123abc",
      "client_secret": "456def",
      "scope": "email,user_posts",
      "apiVersion": "v4.0"
    },
    {
      "app_name": "MyApp",
      "app_key": "myapp123",
      "provider_name": "google",
      "client_id": "789ghi",
      "client_secret": "101jkl",
      "scope": "openid",
      "apiVersion": ""
    }
  ]
}
```

### ファイルの検証
ファイルを配置した後、アプリケーションを再起動して、設定が正しく読み込まれていることを確認してください。問題がある場合は、設定ファイルの構文と値を再確認してください。

## 使い方

JavaScriptから簡単にSNS認証をトリガーすることができます。以下のスクリプトをWebページに追加してください。

```html
<script src="your-domain/austin/app/austin.js"></script>
<script>
const austin = new Austin("your-domain", "your-app-key");
austin.popup("twitter", function(data) {
    if (data.status == 'ok') {
        console.log(data.provider);
        console.log(data.id);
        console.log(data.oauthToken);
        console.log(data.oauthTokenSecret);
    } else if (data.status == 'ng') {
        console.log(data.errorMessage);
    }
});
</script>
```

## プロジェクトに貢献する

- このプロジェクトはオープンソースであり、誰でも改善提案やプルリクエストを歓迎します。
- [バグ報告](https://github.com/livlog-llc/austin/issues)
- [機能リクエスト](https://github.com/livlog-llc/austin/issues)
- [プルリクエスト](https://github.com/livlog-llc/austin/pulls)

## ライセンス

このプロジェクトはMIT Licenseのもとで公開されています。
