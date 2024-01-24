package jp.livlog.austin.helper.discord;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Generated ("jsonschema2pojo")
@Data
public class User {

    @SerializedName ("id")
    @Expose
    private String  id;

    @SerializedName ("username")
    @Expose
    private String  username;

    @SerializedName ("avatar")
    @Expose
    private String  avatar;

    @SerializedName ("discriminator")
    @Expose
    private String  discriminator;

    @SerializedName ("public_flags")
    @Expose
    private Integer publicFlags;

    @SerializedName ("premium_type")
    @Expose
    private Integer premiumType;

    @SerializedName ("flags")
    @Expose
    private Integer flags;

    @SerializedName ("banner")
    @Expose
    private Object  banner;

    @SerializedName ("accent_color")
    @Expose
    private Object  accentColor;

    @SerializedName ("global_name")
    @Expose
    private String  globalName;

    @SerializedName ("avatar_decoration_data")
    @Expose
    private Object  avatarDecorationData;

    @SerializedName ("banner_color")
    @Expose
    private Object  bannerColor;

    @SerializedName ("mfa_enabled")
    @Expose
    private Boolean mfaEnabled;

    @SerializedName ("locale")
    @Expose
    private String  locale;

}
