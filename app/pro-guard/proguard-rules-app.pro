# Prougard rules for all modules goes here.

#Keep model, but still obfuscate
-keep,allowobfuscation class **.model.** { *; }

# Repack in 'd'
-repackageclasses 'd'

# Preserve the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# Keep the line number information, hide the original source file name.
-renamesourcefileattribute SourceFile

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}
