/*
 * SPDX-FileCopyrightText: The Android Open Source Project
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

#pragma once

#include <aidl/android/media/audio/BnHalAdapterVendorExtension.h>

namespace vendor::audio::parserservice {

/*
 * IHalAdapterVendorExtension for the MediaTek AIDL audio HAL.
 *
 * Mirrors the behaviour of the stock MediaTek parser service: raw key/value
 * strings are passed to the HAL unchanged as the id of a single
 * VendorParameter, which MTKAdapterLayer::setVendorParameters() hands to the
 * legacy setParameters() path. Built against the current interface, where
 * ParameterScope became a parcelable.
 */
class ParameterParser : public ::aidl::android::media::audio::BnHalAdapterVendorExtension {
  public:
    ParameterParser() = default;

  protected:
    ::ndk::SpAIBinder createBinder() override;

  private:
    ::ndk::ScopedAStatus parseVendorParameterIds(
            const ::aidl::android::media::audio::IHalAdapterVendorExtension::ParameterScope&
                    in_scope,
            const std::string& in_rawKeys, std::vector<std::string>* _aidl_return) override;

    ::ndk::ScopedAStatus parseVendorParameters(
            const ::aidl::android::media::audio::IHalAdapterVendorExtension::ParameterScope&
                    in_scope,
            const std::string& in_rawKeysAndValues,
            std::vector<::aidl::android::hardware::audio::core::VendorParameter>*
                    out_syncParameters,
            std::vector<::aidl::android::hardware::audio::core::VendorParameter>*
                    out_asyncParameters) override;

    ::ndk::ScopedAStatus parseBluetoothA2dpReconfigureOffload(
            const std::string& in_rawValue,
            std::vector<::aidl::android::hardware::audio::core::VendorParameter>* _aidl_return)
            override;

    ::ndk::ScopedAStatus parseBluetoothLeReconfigureOffload(
            const std::string& in_rawValue,
            std::vector<::aidl::android::hardware::audio::core::VendorParameter>* _aidl_return)
            override;

    ::ndk::ScopedAStatus processVendorParameters(
            const ::aidl::android::media::audio::IHalAdapterVendorExtension::ParameterScope&
                    in_scope,
            const std::vector<::aidl::android::hardware::audio::core::VendorParameter>&
                    in_parameters,
            std::string* _aidl_return) override;
};

}  // namespace vendor::audio::parserservice
