package jp.livlog.austin.data;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

/**
 * プロバイダデータ.
 *
 * @author H.Aoshima
 * @version 1.0
 *
 */
@Data
@Generated ("jsonschema2pojo")
public class Provider {

    @SerializedName ("provider_name")
    @Expose
    String providerName;

    @SerializedName ("client_id")
    @Expose
    String clientId;

    @SerializedName ("client_secret")
    @Expose
    String clientSecret;

    @SerializedName ("scope")
    @Expose
    String scope;

    @SerializedName ("apiVersion")
    @Expose
    String apiVersion;

}