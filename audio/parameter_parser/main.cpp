/*
 * SPDX-FileCopyrightText: The Android Open Source Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

#define LOG_TAG "Audio_ParameterParser"

#include <android-base/logging.h>
#include <android/binder_manager.h>
#include <android/binder_process.h>

#include "ParameterParser.h"

using vendor::audio::parserservice::ParameterParser;

int main() {
    const std::string parserFqn =
            std::string(::aidl::android::media::audio::IHalAdapterVendorExtension::descriptor)
                    .append("/default");
    auto parser = ndk::SharedRefBase::make<ParameterParser>();
    binder_status_t status = AServiceManager_addService(parser->asBinder().get(), parserFqn.c_str());
    if (status != STATUS_OK) {
        LOG(ERROR) << "failed to register service for \"" << parserFqn << "\"";
        return EXIT_FAILURE;
    }
    ABinderProcess_joinThreadPool();
    return EXIT_FAILURE;  // should not reach
}
