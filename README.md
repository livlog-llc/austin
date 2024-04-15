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
