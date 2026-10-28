## ViPER4Android FX RE

Integrate V4A by completing these three steps:

1.  **Build System:** Add the config to **`device.mk`**:

    ```makefile
    $(call inherit-product, device/xiaomi/rodin/viper4android/config.mk)
    ```

    If you're using AIDL audio service, then flag this after call block:
    ```mk
    TARGET_USES_VIPER_AIDL := true
    ```

2.  **Audio Effects:** Add these lines to the `<libraries>` block in **`audio_effects_config.xml`** (`/vendor/etc/` or `/etc/`):

    ```xml
    <library name="v4a_aidl" path="libv4a_aidl.so"/>
    <effect name="v4a_standard_aidl" library="v4a_aidl" uuid="90380da3-8536-4744-a6a3-5731970e640f" type="7261676f-6d75-7369-6364-28e2fd3ac39e"/>
    ```

    If you're using HIDL audio effects, then modify audio_effects.xml instead and follow-up the code below:
    ```xml
    <library name="v4a_re" path="libv4a_re.so"/>
    <effect name="v4a_standard_re" library="v4a_re" uuid="90380da3-8536-4744-a6a3-5731970e640f"/>
    ``` 
