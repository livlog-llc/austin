package jp.livlog.austin.data;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

/**
 * 設定データ.
 *
 * @author H.Aoshima
 * @version 1.0
 *
 */
@Data
public class Setting {

    @SerializedName ("app_name")
    @Expose
    String          appName;

    @SerializedName ("app_key")
    @Expose
    String          appKey;

    @SerializedName ("domains")
    @Expose
    List <String>   domains   = null;

    @SerializedName ("providers")
    @Expose
    List <Provider> providers = null;

}
