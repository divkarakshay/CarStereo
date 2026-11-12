# Add project specific ProGuard rules here.
# By default, the flags are added to the default ProGuard flags.
# For more information, read the ProGuard manual:
#   http://proguard.sourceforge.net/manual.html

-keepattributes *Annotation*
-keepclassmembers public class * {
    public static void main(java.lang.String[]);
}

# Keep the serial port related classes
-keep class com.cardumper.** { *; }