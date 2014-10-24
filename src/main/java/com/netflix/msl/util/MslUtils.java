/**
 * Copyright (c) 2013-2014 Netflix, Inc.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.msl.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.bind.DatatypeConverter;

import com.netflix.msl.MslConstants.CompressionAlgorithm;
import com.netflix.msl.MslError;
import com.netflix.msl.MslException;
import com.netflix.msl.io.LZWInputStream;
import com.netflix.msl.io.LZWOutputStream;

/**
 * Utility methods.
 * 
 * @author Wesley Miaw <wmiaw@netflix.com>
 */
public class MslUtils {
    /**
     * Compress the provided data using the specified compression algorithm.
     * 
     * @param compressionAlgo the compression algorithm.
     * @param data the data to compress.
     * @return the compressed data.
     * @throws MslException if there is an error compressing the data.
     */
    public static byte[] compress(final CompressionAlgorithm compressionAlgo, final byte[] data) throws MslException {
        try {
            switch (compressionAlgo) {
                case GZIP:
                {
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length);
                    final GZIPOutputStream gzos = new GZIPOutputStream(baos);
                    gzos.write(data);
                    gzos.close();
                    return baos.toByteArray();
                }
                case LZW:
                {
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length);
                    final LZWOutputStream lzwos = new LZWOutputStream(baos);
                    lzwos.write(data);
                    lzwos.close();
                    return baos.toByteArray();
                }
                default:
                    throw new MslException(MslError.UNSUPPORTED_COMPRESSION, compressionAlgo.name());
            }
        } catch (final IOException e) {
            final String dataB64 = DatatypeConverter.printBase64Binary(data);
            throw new MslException(MslError.COMPRESSION_ERROR, "algo " + compressionAlgo.name() + " data " + dataB64, e);
        }
    }
    
    /**
     * Uncompress the provided data using the specified compression algorithm.
     * 
     * @param compressionAlgo the compression algorithm.
     * @param data the data to uncompress.
     * @return the uncompressed data.
     * @throws MslException if there is an error uncompressing the data.
     */
    public static byte[] uncompress(final CompressionAlgorithm compressionAlgo, final byte[] data) throws MslException {
        try {
            switch (compressionAlgo) {
                case GZIP:
                {
                    final ByteArrayInputStream bais = new ByteArrayInputStream(data);
                    final GZIPInputStream gzis = new GZIPInputStream(bais);
                    final byte[] buffer = new byte[data.length];
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length);
                    do {
                        final int bytesRead = gzis.read(buffer);
                        if (bytesRead == -1) break;
                        baos.write(buffer, 0, bytesRead);
                    } while (true);
                    return baos.toByteArray();
                }
                case LZW:
                {
                    final ByteArrayInputStream bais = new ByteArrayInputStream(data);
                    final LZWInputStream lzwis = new LZWInputStream(bais);
                    try {
                        final byte[] buffer = new byte[data.length];
                        final ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length);
                        do {
                            final int bytesRead = lzwis.read(buffer);
                            if (bytesRead == -1) break;
                            baos.write(buffer, 0, bytesRead);
                        } while (true);
                        return baos.toByteArray();
                    } finally {
                        lzwis.close();
                    }
                }
                default:
                    throw new MslException(MslError.UNSUPPORTED_COMPRESSION, compressionAlgo.name());
            }
        } catch (final IOException e) {
            final String dataB64 = DatatypeConverter.printBase64Binary(data);
            throw new MslException(MslError.UNCOMPRESSION_ERROR, "algo " + compressionAlgo.name() + " data " + dataB64, e);
        }
    }
}