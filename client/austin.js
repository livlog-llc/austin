class Austin {

    constructor(austinURL, appKey) {
        this.austinURL = austinURL;
        this.appKey = appKey;
    }

    popup(provider, action) {
        const url = this.austinURL + "/austin/oauth/" + provider + "/" + this.appKey;
        // 返りのアクションを設定
        addEventListener('message', function(event) {
            // 受け取ってからの処理
            // 送られてきたのが確実に「c-brains.co.jp」からである事を確認
            // if (event.origin != "http://localhost:8080") {
            //   return;
            // }
            action(JSON.parse(event.data));
        }, false);
        // 子画面をオープン
        window.open(
            url,
            "austin",
            "width=1200, height=600, personalbar=0, toolbar=0, scrollbars=1, resizable=!"
        );
    }
}