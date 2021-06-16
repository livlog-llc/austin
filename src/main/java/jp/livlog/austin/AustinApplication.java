package jp.livlog.austin;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;

import jp.livlog.austin.resource.CallbackResource;
import jp.livlog.austin.resource.OAuthResource;

/**
 * アプリケーションクラス.
 *
 * @author H.Aoshima
 * @version 1.0
 */
public class AustinApplication extends Application {

    /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public synchronized Restlet createInboundRoot() {

        final var router = new Router(this.getContext());

        final var dir = new Directory(this.getContext(), "war:///app");

        router.attach("/app", dir);
        router.attach("/oauth/{provider}/{app_key}", OAuthResource.class);
        router.attach("/callback/{provider}/{app_key}", CallbackResource.class);

        return router;
    }

    // public static void main(String[] args) throws Exception {
    //
    // // Create a new Component.
    // final var component = new Component();
    //
    // // Add a new HTTP server listening on port 8080.
    // component.getServers().add(Protocol.HTTP, 8080);
    //
    // // Attach the austin application.
    // component.getDefaultHost().attach("/austin",
    // new AustinApplication());
    //
    // // Start the component.
    // component.start();
    // }
}