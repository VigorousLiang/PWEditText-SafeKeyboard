LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_CFLAGS += \
  -DANDROID_NDK \

LOCAL_SRC_FILES := $(LOCAL_PATH)/IJniInterface.cpp

LOCAL_CFLAGS += -ffunction-sections -fdata-sections -fvisibility=hidden
LOCAL_CPPFLAGS += -ffunction-sections -fdata-sections -fvisibility=hidden

LOCAL_LDLIBS    += -llog
LOCAL_LDFLAGS += -Wl,--gc-sections
LOCAL_MODULE := securityKey
include $(BUILD_SHARED_LIBRARY)