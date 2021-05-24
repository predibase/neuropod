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

package com.uber.neuropod;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.Buffer;
import java.nio.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import sun.misc.Unsafe;

/**
 * The factory type for NeuropodTensor, can be obtained from a neuropod model or as a generic allocator.
 * Should call close method when finish using it.
 */
public class NeuropodTensorAllocator extends NativeClass {
    protected NeuropodTensorAllocator(long handle) {
        super(handle);
    }

    private static Unsafe unsafe = null;
    private static long addressOffset;
    static {
        try {
            final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafe = (Unsafe) unsafeField.get(null);
            addressOffset = unsafe.objectFieldOffset(Buffer.class.getDeclaredField("address"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static long getAddress(ByteBuffer buffy) {
        // steal the value of address field from Buffer, holding the memory address for this ByteBuffer
        return unsafe.getLong(buffy, addressOffset);
    }

    public static ByteBuffer allocateAlignedByteBuffer(int capacity, long align) {
        // Power of 2 --> single bit, none power of 2 alignments are not allowed.
        if (Long.bitCount(align) != 1) {
            throw new IllegalArgumentException("Alignment must be a power of 2");
        }
        // We over allocate by the alignment so we know we can have a large enough aligned
        // block of memory to use. Also set order to native while we are here.
        ByteBuffer allocatedBuffer = ByteBuffer.allocateDirect((int) (capacity + align));
        long address = getAddress(allocatedBuffer);

        // check if the address is already aligned
        // Because JDK11 is used to build but 8 at runtime, there is a problem with ByteBuffer methods:
        // https://github.com/eclipse/jetty.project/issues/3244
        // This is know problem https://engwiki.uberinternal.com/display/TE0DEVEXP/%5BFAQ%5D+Migrating+to+Java+11
        Buffer buffy = (Buffer) allocatedBuffer;
        if ((address & (align - 1)) == 0) {
            buffy.limit(capacity);
            return allocatedBuffer.slice().order(ByteOrder.nativeOrder());
        } else {
            // shift the start position to an aligned address --> address + (align - (address % align))
            // the formula is valid for power of 2 values only
            int newPosition = (int) (align - (address & (align - 1)));
            buffy.position(newPosition);
            int newLimit = newPosition + capacity;
            buffy.limit(newLimit);
            return allocatedBuffer.slice().order(ByteOrder.nativeOrder());
        }
    }

    private static final Set<TensorType> SUPPORTED_TENSOR_TYPES = new HashSet<>(Arrays.asList(
            TensorType.INT32_TENSOR,
            TensorType.INT64_TENSOR,
            TensorType.DOUBLE_TENSOR,
            TensorType.FLOAT_TENSOR));


    public Set<TensorType> getSupportedTensorTypes() {
        return SUPPORTED_TENSOR_TYPES;
    }

    /**
     * Create a NeuropodTensor based on given ByteBuffer, dims array and tensorType. The created tensor
     * will have the input tensorType.
     * <p>
     * For the sake of performance we require that the input buffer is direct, native and not read-only
     * because in this case Java virtual machine will perform native I/O operations directly upon it.
     * A direct byte buffer may be created by invoking the allocateDirect factory method of this class.
     * Will not trigger a copy.
     *
     * @param byteBuffer the buffer that contains tensor data
     * @param dims       the shape of the tensor
     * @param tensorType the tensor type
     * @return the created NeuropodTensor
     */
    public NeuropodTensor tensorFromMemory(ByteBuffer byteBuffer, long[] dims, TensorType tensorType) {
        if (!SUPPORTED_TENSOR_TYPES.contains(tensorType)) {
            throw new NeuropodJNIException("unsupported tensor type: " + tensorType.name());
        }
        if (!byteBuffer.isDirect()) {
            throw new NeuropodJNIException("the input byteBuffer is not direct");
        }
        if (byteBuffer.order() != ByteOrder.nativeOrder()) {
            throw new NeuropodJNIException("the input byteBuffer is not in a native order");
        }
        if (byteBuffer.isReadOnly()) {
            throw new NeuropodJNIException("the input byteBuffer is read-only");
        }
        return createTensorFromBuffer(byteBuffer, dims, tensorType);
    }

    /**
     * Create a NeuropodTensor based on given LongBuffer, dims array. The created tensor
     * will have tensor type INT64_TENSOR.
     * <p>
     *
     * @param longBuffer the buffer that contains tensor data
     * @param dims       the shape of the tensor
     * @return the created NeuropodTensor
     */
    public NeuropodTensor copyFrom(LongBuffer longBuffer, long[] dims) {
        TensorType type = TensorType.INT64_TENSOR;
        ByteBuffer tensorBuffer = allocateJavaBuffer(dims, type);
        tensorBuffer.asLongBuffer().put(longBuffer);
        return createTensorFromBuffer(tensorBuffer, dims, type);
    }

    /**
     * Create a NeuropodTensor based on given IntBuffer, dims array. The created tensor
     * will have type INT32_TENSOR.
     * <p>
     *
     * @param intBuffer the buffer that contains tensor data
     * @param dims      the shape of the tensor
     * @return the created NeuropodTensor
     */
    public NeuropodTensor copyFrom(IntBuffer intBuffer, long[] dims) {
        TensorType type = TensorType.INT32_TENSOR;
        ByteBuffer tensorBuffer = allocateJavaBuffer(dims, type);
        tensorBuffer.asIntBuffer().put(intBuffer);
        return createTensorFromBuffer(tensorBuffer, dims, type);
    }

    /**
     * Create a NeuropodTensor based on given DoubleBuffer, dims array. The created tensor
     * will have type DOUBLE_TENSOR.
     * <p>
     *
     * @param doubleBuffer the buffer that contains tensor data
     * @param dims         the shape of the tensor
     * @return the created NeuropodTensor
     */
    public NeuropodTensor copyFrom(DoubleBuffer doubleBuffer, long[] dims) {
        TensorType type = TensorType.DOUBLE_TENSOR;
        ByteBuffer tensorBuffer = allocateJavaBuffer(dims, type);
        tensorBuffer.asDoubleBuffer().put(doubleBuffer);
        return createTensorFromBuffer(tensorBuffer, dims, type);
    }

    /**
     * Create a NeuropodTensor based on given FloatBuffer, dims array. The created tensor
     * will have type FLOAT_TENSOR.
     * <p>
     *
     * @param floatBuffer the buffer that contains tensor data
     * @param dims        the shape of the tensor
     * @return the neuropod NeuropodTensor
     */
    public NeuropodTensor copyFrom(FloatBuffer floatBuffer, long[] dims) {
        TensorType type = TensorType.FLOAT_TENSOR;
        ByteBuffer tensorBuffer = allocateJavaBuffer(dims, type);
        tensorBuffer.asFloatBuffer().put(floatBuffer);
        return createTensorFromBuffer(tensorBuffer, dims, type);
    }

    /**
     * Create a NeuropodTensor based on given string list, dims array. The created tensor
     * will have type STRING_TENSOR.
     * <p>
     *
     * @param stringList the string list
     * @param dims       the dims
     * @return the created NeuropodTensor
     */
    public NeuropodTensor copyFrom(List<String> stringList, long[] dims) {
        NeuropodTensor tensor = new NeuropodTensor();
        tensor.setNativeHandle(nativeCreateStringTensor(stringList, dims, super.getNativeHandle()));
        return tensor;
    }

    private static ByteBuffer allocateJavaBuffer(long[] dims, TensorType tensorType) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(
                (int)(tensorType.getBytesPerElement() *
                        Arrays.stream(dims).reduce(1, (e1, e2) -> e1*e2)));
        return buffer.order(ByteOrder.nativeOrder());
    }

    private NeuropodTensor createTensorFromBuffer(ByteBuffer buffer, long[] dims, TensorType tensorType) {
        NeuropodTensor tensor = new NeuropodTensor();
        tensor.buffer = buffer;
        tensor.setNativeHandle(nativeAllocate(dims, tensorType.getValue(), tensor.buffer, super.getNativeHandle()));
        return tensor;
    }

    private static native long nativeAllocate(long[] dims,
                                              int tensorType,
                                              ByteBuffer buffer,
                                              long allocatorHandle) throws NeuropodJNIException;

    private static native long nativeCreateStringTensor(List<String> data,
                                                        long[] dims,
                                                        long allocatorHandle) throws NeuropodJNIException;

    @Override
    protected native void nativeDelete(long handle) throws NeuropodJNIException;
}
