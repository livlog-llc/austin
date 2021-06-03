package jp.livlog.austin.resource;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import lombok.extern.slf4j.Slf4j;

/**
 * Resource which has only one representation.
 */
@Slf4j
public class CallbackResource extends ServerResource {

    @Get
    public String represent() {

        CallbackResource.log.info("ログ出力テスト");

        return "hello, world";
    }

}