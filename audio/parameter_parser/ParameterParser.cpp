/*
 * SPDX-FileCopyrightText: The Android Open Source Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

#define LOG_TAG "Audio_ParameterParser"

#include "ParameterParser.h"

#include <android-base/logging.h>
#include <android/binder_ibinder_platform.h>
#include <android/binder_to_string.h>
#include <unistd.h>

#include <string>

namespace vendor::audio::parserservice {

using ::aidl::android::hardware::audio::core::VendorParameter;
using ParameterScope = ::aidl::android::media::audio::IHalAdapterVendorExtension::ParameterScope;

namespace {

constexpr char kBinderThreadVip[] = "BinderThread_VIP";

VendorParameter makeRawParameter(const std::string& raw) {
    VendorParameter param;
    param.id = raw;
    return param;
}

}  // namespace

::ndk::SpAIBinder ParameterParser::createBinder() {
    ::ndk::SpAIBinder binder = BnHalAdapterVendorExtension::createBinder();
    AIBinder_setInheritRt(binder.get(), true);
    return binder;
}

::ndk::ScopedAStatus ParameterParser::parseVendorParameterIds(const ParameterScope& in_scope,
                                                              const std::string& in_rawKeys,
                                                              std::vector<std::string>* _aidl_return) {
    LOG(DEBUG) << __func__ << ": scope: " << in_scope.toString() << ", keys: " << in_rawKeys;
    _aidl_return->assign({in_rawKeys});
    return ::ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus ParameterParser::parseVendorParameters(
        const ParameterScope& in_scope, const std::string& in_rawKeysAndValues,
        std::vector<VendorParameter>* out_syncParameters, std::vector<VendorParameter>*) {
    LOG(DEBUG) << __func__ << ": scope: " << in_scope.toString()
               << ", keys/values: " << in_rawKeysAndValues;
    out_syncParameters->push_back(makeRawParameter(in_rawKeysAndValues));
    if (in_rawKeysAndValues.find(kBinderThreadVip) != std::string::npos) {
        const std::string vip = std::string(kBinderThreadVip) + "= " + std::to_string(getpid());
        LOG(DEBUG) << __func__ << ": set another parameter, set ParserService VIP: " << vip;
        out_syncParameters->push_back(makeRawParameter(vip));
    }
    return ::ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus ParameterParser::parseBluetoothA2dpReconfigureOffload(
        const std::string& in_rawValue, std::vector<VendorParameter>*) {
    LOG(DEBUG) << __func__ << ": value: " << in_rawValue;
    return ::ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus ParameterParser::parseBluetoothLeReconfigureOffload(
        const std::string& in_rawValue, std::vector<VendorParameter>*) {
    LOG(DEBUG) << __func__ << ": value: " << in_rawValue;
    return ::ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus ParameterParser::processVendorParameters(
        const ParameterScope& in_scope, const std::vector<VendorParameter>& in_parameters,
        std::string* _aidl_return) {
    LOG(DEBUG) << __func__ << ": scope: " << in_scope.toString()
               << ", parameters: " << ::android::internal::ToString(in_parameters);
    if (!in_parameters.empty()) {
        *_aidl_return = in_parameters.front().id;
    }
    return ::ndk::ScopedAStatus::ok();
}

}  // namespace vendor::audio::parserservice
