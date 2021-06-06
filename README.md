# austin

アプリキーの生成に利用。

パスワード生成

https://www.luft.co.jp/cgi/randam.php


    <script src="https://livlog.xyz/austin/app/austin.js"></script>

    <script>
      $(function(){
        $('#openBtn').click(function(event) {
          const austin = new Austin("https://livlog.xyz", "kqNxNdN4F9aZ");
          austin.popup("twitter", function(data) {
            console.log(data);
          });
        });
      });
    </script>