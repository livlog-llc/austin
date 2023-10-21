class Austin {
    constructor(austinURL, appKey) {
        this.austinURL = austinURL;
        this.appKey = appKey;
    }

    popup(provider, action) {
        const uuid = this.generateUuid();
        const url = `${this.austinURL}/austin/oauth/${provider}/${this.appKey}?key=${uuid}`;

        const messageHandler = (event) => {
            if (event.origin !== this.austinURL) return;
            action(JSON.parse(event.data));
            window.removeEventListener('message', messageHandler);
        };

        window.addEventListener('message', messageHandler, false);

        window.open(
            url,
            "austin",
            "width=1200, height=600, personalbar=0, toolbar=0, scrollbars=1, resizable=1"
        );
    }

    async get(uuid) {
        const url = `${this.austinURL}/austin/result/${uuid}`;
        const response = await fetch(url);
        if (!response.ok) throw new Error("Failed to fetch data");
        return response.json();
    }

    generateUuid() {
        const chars = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".split("");
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
