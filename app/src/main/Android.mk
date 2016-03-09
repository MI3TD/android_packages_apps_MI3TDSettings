LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_RESOURCE_DIR += frameworks/opt/datetimepicker/res

ifeq ($(TARGET_BUILD_APPS),)
LOCAL_RESOURCE_DIR += frameworks/support/v7/appcompat/res
else
LOCAL_RESOURCE_DIR += prebuilts/sdk/current/support/v7/appcompat/res
endif

LOCAL_MODULE_TAGS := optional

LOCAL_PACKAGE_NAME := MI3TDSettings

LOCAL_SRC_FILES := $(call all-java-files-under, java)

LOCAL_STATIC_JAVA_LIBRARIES += android-support-v4
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v7-appcompat

LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages android.support.v7.appcompat

LOCAL_PRIVILEGED_MODULE := true

include $(BUILD_PACKAGE)
