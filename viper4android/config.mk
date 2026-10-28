PRODUCT_PACKAGES += \
    ViPER4Android \
    privapp-permissions-viper4android.xml \

ifeq (true,$(TARGET_USES_VIPER_AIDL))
PRODUCT_PACKAGES += \
    libv4a_aidl
else
PRODUCT_PACKAGES += \
    libv4a_re
endif

BOARD_SEPOLICY_DIRS += device/xiaomi/rodin/viper4android/sepolicy/vendor

