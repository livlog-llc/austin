# Austin
「**Austin**」は、OAuth認証のコーディングの煩わしさを省くために主要なSNSログインの動作を流用できるように作成しました。

## どんなプロジェクトですか？
これは Java11 に基づくソリューションであり、オープンソースとして、Tomcatの Webサーバをセットアップして実行できます。

## 主な特徴
※ドキュメント作成中
現時点では「Twitter」「Facebook」「Line」に対応


## アプリキーの生成
アプリキーの生成には以下のサイトでパスワードを生成して利用すると良いかと思います。

パスワード生成（パスワード作成）ツール<br>
https://www.luft.co.jp/cgi/randam.php

## 使い方

### Javascriptの設定

    <script src="https://livlog.xyz/austin/app/austin.js"></script>
    <script>
    const austin = new Austin("https://livlog.xyz", "kqNxNdN4F9aZ");
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
