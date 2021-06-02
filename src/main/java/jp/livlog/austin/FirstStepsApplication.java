package jp.livlog.austin;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;

import jp.livlog.austin.resource.HelloWorldResource;

public class FirstStepsApplication extends Application {

    /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public synchronized Restlet createInboundRoot() {

        // Create a router Restlet that routes each call to a new instance of HelloWorldResource.
        final var router = new Router(this.getContext());

        // Defines only one route
        router.attach("/hello", HelloWorldResource.class);

        return router;
    }


    public static void main(String[] args) throws Exception {

        // Create a new Component.
        final var component = new Component();

        // Add a new HTTP server listening on port 8182.
        component.getServers().add(Protocol.HTTP, 8080);

        // Attach the sample application.
        component.getDefaultHost().attach("/austin",
                new FirstStepsApplication());

        // Start the component.
        component.start();
    }
}