/* Copyright (c) 2020 UATC, LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

#pragma once

#include "neuropod/neuropod.hh"

#include <memory>
#include <string>
#include <vector>

#include <jni.h>

namespace neuropod
{
namespace jni
{

// Convert jstring to UTF-8 encoded cpp string
std::string to_string(JNIEnv *env, jstring target);

// Convert UTF-8 encoded cpp string to a jstring
jstring to_jstring(JNIEnv *env, const std::string &source);

// A wrapper for env->FindClass, will throw a cpp exception if the find fails.
jclass find_class(JNIEnv *env, const std::string &name);

// A wrapper for env->GetMethodID, will throw a cpp exception if the get fails.
jmethodID get_method_id(JNIEnv *env, jclass clazz, const std::string &name, const std::string &sig);

// Get the corresponding Java TensorType enum based on the given field name.
jobject get_tensor_type_field(JNIEnv *env, const std::string &fieldName);

// Convert cpp tensor type to string
std::string tensor_type_to_string(TensorType type);

// Copy a shared_ptr to heap
template <typename T>
std::shared_ptr<T> *toHeap(std::shared_ptr<T> ptr)
{
    return new std::shared_ptr<T>(ptr);
}

// Create a direct buffer for the tensor data
template <typename T>
jobject createDirectBuffer(JNIEnv *env, NeuropodTensor *tensor);

// Throw a Java NeuropodJNIException exception after the JNI call have finished.
// If there are multiple throw_java_exception calls during a C++ function, only the
// last one is effective.
void throw_java_exception(JNIEnv *env, const std::string &message);

using string_accessor_type =
    StringProxy<TypedNeuropodTensor<std::basic_string<char, std::char_traits<char>, std::allocator<char>>>>;

} // namespace jni
} // namespace neuropod
#include "utils_impl.h"
