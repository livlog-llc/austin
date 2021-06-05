package jp.livlog.austin.resource;

import java.io.IOException;
import java.util.Map;

import org.restlet.resource.Get;

import jp.livlog.austin.data.Setting;
import jp.livlog.austin.share.AbsBaseResource;
import lombok.extern.slf4j.Slf4j;

/**
 * Resource which has only one representation.
 */
@Slf4j
public class CallbackResource extends AbsBaseResource {

    @Get
    public String represent() throws IOException {

        Setting setting = getSetting();

        Map <String, Object> attrMap = getRequestAttributes();
        String provider = (String) attrMap.get("provider");
        String appKey = (String) attrMap.get("app_key");

        CallbackResource.log.info("ログ出力テスト");

        return "hello, world";
    }

}