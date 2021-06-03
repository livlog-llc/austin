package jp.livlog.austin.resource;

import org.restlet.resource.Get;

import jp.livlog.austin.share.AbsAustinResource;
import lombok.extern.slf4j.Slf4j;

/**
 * Resource which has only one representation.
 */
@Slf4j
public class CallbackResource extends AbsAustinResource {

    @Get
    public String represent() {

        CallbackResource.log.info("ログ出力テスト");

        return "hello, world";
    }

}