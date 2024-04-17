-keep public class com.core.bridge.callback.CallbackBean {*;}

-keep public class com.core.bridge.** {*;}
-keep @com.core.bridge.annotation.JavascriptBridge class * {*;}
-keepclassmembers class * {
    @android.webkit.KeepNotProguard <methods>;
}