package jp.livlog.austin.helper.discord;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.Gson;

public class DiscordUserClient {

    private final String     token;

    private final HttpClient client;

    public DiscordUserClient(final String token) {

        this.token = token;
        this.client = HttpClient.newBuilder().version(Version.HTTP_2).build();
    }


    public User getUserInfo() throws Exception {

        final var request = HttpRequest.newBuilder()
                .uri(URI.create("https://discord.com/api/v9/users/@me"))
                .header("Authorization", "Bearer " + this.token)
                .GET()
                .build();

        final HttpResponse <String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

        final var gson = new Gson();
        final var user = gson.fromJson(response.body(), User.class);

        return user;
    }


    public static void main(final String[] args) {

        try {
            final var discordClient = new DiscordUserClient("あなたのトークン");
            final var userInfo = discordClient.getUserInfo();
            System.out.println(userInfo);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
