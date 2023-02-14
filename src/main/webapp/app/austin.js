class Austin {

    constructor(austinURL, appKey) {
        Austin.austinURL = austinURL;
        Austin.appKey = appKey;
    }

    popup(provider, action) {
        
        const url = Austin.austinURL + "/austin/oauth/" + provider + "/" + Austin.appKey + "?key=" + this.generateUuid();

        // 返りのアクションを設定
        addEventListener('message', function(event) {
            // 受け取ってからの処理
            // 送られてきたのが確実に「c-brains.co.jp」からである事を確認
            if (event.origin != Austin.austinURL) {
              return;
            }
            action(JSON.parse(event.data));
        }, false);
        // 子画面をオープン
        const openWindow = window.open(
            url,
            "austin",
            "width=1200, height=600, personalbar=0, toolbar=0, scrollbars=1, resizable=!"
        );
        openWindow.addEventListener('load',function(event){
            console.log('opne');
            openWindow.addEventListener('unload',function(event){
                console.log('close');
            }, false);
        }, false);
    }
    
    generateUuid() {
        // https://github.com/GoogleChrome/chrome-platform-analytics/blob/master/src/internal/identifier.js
        // const FORMAT: string = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx";
        let chars = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".split("");
        for (let i = 0, len = chars.length; i < len; i++) {
            switch (chars[i]) {
                case "x":
                    chars[i] = Math.floor(Math.random() * 16).toString(16);
                    break;
                case "y":
                    chars[i] = (Math.floor(Math.random() * 4) + 8).toString(16);
                    break;
            }
        }
        return chars.join("");
    }
}