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

/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_uber_neuropod_Neuropod */

#ifndef _Included_com_uber_neuropod_Neuropod
#define _Included_com_uber_neuropod_Neuropod
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_uber_neuropod_Neuropod
 * Method:    nativeNew
 * Signature: (Ljava/lang/String;J)J
 */
JNIEXPORT jlong JNICALL Java_com_uber_neuropod_Neuropod_nativeNew__Ljava_lang_String_2J(JNIEnv *,
                                                                                        jclass,
                                                                                        jstring,
                                                                                        jlong);

/*
 * Class:     com_uber_neuropod_Neuropod
 * Method:    nativeGetInputs
 * Signature: (J)Ljava/util/List;
 */
JNIEXPORT jobject JNICALL Java_com_uber_neuropod_Neuropod_nativeGetInputs(JNIEnv *, jclass, jlong);

/*
 * Class:     com_uber_neuropod_Neuropod
 * Method:    nativeGetOutputs
 * Signature: (J)Ljava/util/List;
 */
JNIEXPORT jobject JNICALL Java_com_uber_neuropod_Neuropod_nativeGetOutputs(JNIEnv *, jclass, jlong);

/*
 * Class:     com_uber_neuropod_Neuropod
 * Method:    nativeLoadModel
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_uber_neuropod_Neuropod_nativeLoadModel(JNIEnv *, jclass, jlong);

/*
 * Class:     com_uber_neuropod_Neuropod
 * Method:    nativeGetName
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_uber_neuropod_Neuropod_nativeGetName(JNIEnv *, jclass, jlong);

/*
 * Class:     com_uber_neuropod_Neuropod
 * Method:    nativeGetPlatform
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_uber_neuropod_Neuropod_nativeGetPlatform(JNIEnv *, jclass, jlong);

/*
 * Class:     com_uber_neuropod_Neuropod
 * Method:    nativeGetAllocator
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_uber_neuropod_Neuropod_nativeGetAllocator(JNIEnv *, jclass, jlong);

/*
 * Class:     com_uber_neuropod_Neuropod
 * Method:    nativeGetGenericAllocator
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_uber_neuropod_Neuropod_nativeGetGenericAllocator(JNIEnv *, jclass);

/*
 * Class:     com_uber_neuropod_Neuropod
 * Method:    nativeInfer
 * Signature: ([Ljava/lang/Object;Ljava/util/List;J)Ljava/util/Map;
 */
JNIEXPORT jobject JNICALL Java_com_uber_neuropod_Neuropod_nativeInfer(JNIEnv *, jclass, jobjectArray, jobject, jlong);

/*
 * Class:     com_uber_neuropod_Neuropod
 * Method:    nativePrepare
 * Signature: ([Ljava/lang/Object)J
 */
JNIEXPORT jlong JNICALL Java_com_uber_neuropod_Neuropod_nativePrepare(JNIEnv *, jclass, jobjectArray);

/*
 * Class:     com_uber_neuropod_Neuropod
 * Method:    nativeInferPrepared
 * Signature: (J;J)Ljava/util/Map;
 */
JNIEXPORT jobject JNICALL Java_com_uber_neuropod_Neuropod_nativeInferPrepared(JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     com_uber_neuropod_Neuropod
 * Method:    nativeDeletePrepared
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_uber_neuropod_Neuropod_nativeDeletePrepared(JNIEnv *, jclass, jlong);

/*
 * Class:     com_uber_neuropod_Neuropod
 * Method:    nativeDelete
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_uber_neuropod_Neuropod_nativeDelete(JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif
