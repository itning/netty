/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.buffer;

import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.Random;

import static io.netty.buffer.Unpooled.unreleasableBuffer;
import static io.netty.util.ReferenceCountUtil.releaseLater;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ByteBufUtilTest {
    @Test
    public void equalsBufferSubsections() {
        byte[] b1 = new byte[128];
        byte[] b2 = new byte[256];
        Random rand = new Random();
        rand.nextBytes(b1);
        rand.nextBytes(b2);
        final int iB1 = b1.length / 2;
        final int iB2 = iB1 + b1.length;
        final int length = b1.length - iB1;
        System.arraycopy(b1, iB1, b2, iB2, length);
        assertTrue(ByteBufUtil.equals(Unpooled.wrappedBuffer(b1), iB1, Unpooled.wrappedBuffer(b2), iB2, length));
    }

    private static int random(Random r, int min, int max) {
        return r.nextInt((max - min) + 1) + min;
    }

    @Test
    public void notEqualsBufferSubsections() {
        byte[] b1 = new byte[50];
        byte[] b2 = new byte[256];
        Random rand = new Random();
        rand.nextBytes(b1);
        rand.nextBytes(b2);
        final int iB1 = b1.length / 2;
        final int iB2 = iB1 + b1.length;
        final int length = b1.length - iB1;
        System.arraycopy(b1, iB1, b2, iB2, length);
        // Randomly pick an index in the range that will be compared and make the value at that index differ between
        // the 2 arrays.
        int diffIndex = random(rand, iB1, iB1 + length - 1);
        ++b1[diffIndex];
        assertFalse(ByteBufUtil.equals(Unpooled.wrappedBuffer(b1), iB1, Unpooled.wrappedBuffer(b2), iB2, length));
    }

    @Test
    public void notEqualsBufferOverflow() {
        byte[] b1 = new byte[8];
        byte[] b2 = new byte[16];
        Random rand = new Random();
        rand.nextBytes(b1);
        rand.nextBytes(b2);
        final int iB1 = b1.length / 2;
        final int iB2 = iB1 + b1.length;
        final int length = b1.length - iB1;
        System.arraycopy(b1, iB1, b2, iB2, length - 1);
        assertFalse(ByteBufUtil.equals(Unpooled.wrappedBuffer(b1), iB1, Unpooled.wrappedBuffer(b2), iB2,
                Math.max(b1.length, b2.length) * 2));
    }

    @Test (expected = IllegalArgumentException.class)
    public void notEqualsBufferUnderflow() {
        byte[] b1 = new byte[8];
        byte[] b2 = new byte[16];
        Random rand = new Random();
        rand.nextBytes(b1);
        rand.nextBytes(b2);
        final int iB1 = b1.length / 2;
        final int iB2 = iB1 + b1.length;
        final int length = b1.length - iB1;
        System.arraycopy(b1, iB1, b2, iB2, length - 1);
        assertFalse(ByteBufUtil.equals(Unpooled.wrappedBuffer(b1), iB1, Unpooled.wrappedBuffer(b2), iB2,
                -1));
    }

    @Test
    public void testWriteUsAscii() {
        String usAscii = "NettyRocks";
        ByteBuf buf = releaseLater(Unpooled.buffer(16));
        buf.writeBytes(usAscii.getBytes(CharsetUtil.US_ASCII));
        ByteBuf buf2 = releaseLater(Unpooled.buffer(16));
        ByteBufUtil.writeAscii(buf2, usAscii);

        assertEquals(buf, buf2);
    }

    @Test
    public void testWriteUsAsciiWrapped() {
        String usAscii = "NettyRocks";
        ByteBuf buf = unreleasableBuffer(releaseLater(Unpooled.buffer(16)));
        assertWrapped(buf);
        buf.writeBytes(usAscii.getBytes(CharsetUtil.US_ASCII));
        ByteBuf buf2 = unreleasableBuffer(releaseLater(Unpooled.buffer(16)));
        assertWrapped(buf2);
        ByteBufUtil.writeAscii(buf2, usAscii);

        assertEquals(buf, buf2);
    }

    @Test
    public void testWriteUtf8() {
        String usAscii = "Some UTF-8 like ???????????";
        ByteBuf buf = releaseLater(Unpooled.buffer(16));
        buf.writeBytes(usAscii.getBytes(CharsetUtil.UTF_8));
        ByteBuf buf2 = releaseLater(Unpooled.buffer(16));
        ByteBufUtil.writeUtf8(buf2, usAscii);

        assertEquals(buf, buf2);
    }

    @Test
    public void testWriteUtf8Surrogates() {
        // leading surrogate + trailing surrogate
        String surrogateString = new StringBuilder(2)
                                .append('a')
                                .append('\uD800')
                                .append('\uDC00')
                                .append('b')
                                .toString();
        ByteBuf buf = releaseLater(Unpooled.buffer(16));
        buf.writeBytes(surrogateString.getBytes(CharsetUtil.UTF_8));
        ByteBuf buf2 = releaseLater(Unpooled.buffer(16));
        ByteBufUtil.writeUtf8(buf2, surrogateString);

        assertEquals(buf, buf2);
    }

    @Test
    public void testWriteUtf8InvalidOnlyTrailingSurrogate() {
        String surrogateString = new StringBuilder(2)
                                .append('a')
                                .append('\uDC00')
                                .append('b')
                                .toString();
        ByteBuf buf = releaseLater(Unpooled.buffer(16));
        buf.writeBytes(surrogateString.getBytes(CharsetUtil.UTF_8));
        ByteBuf buf2 = releaseLater(Unpooled.buffer(16));
        ByteBufUtil.writeUtf8(buf2, surrogateString);

        assertEquals(buf, buf2);
    }

    @Test
    public void testWriteUtf8InvalidOnlyLeadingSurrogate() {
        String surrogateString = new StringBuilder(2)
                                .append('a')
                                .append('\uD800')
                                .append('b')
                                .toString();
        ByteBuf buf = releaseLater(Unpooled.buffer(16));
        buf.writeBytes(surrogateString.getBytes(CharsetUtil.UTF_8));
        ByteBuf buf2 = releaseLater(Unpooled.buffer(16));
        ByteBufUtil.writeUtf8(buf2, surrogateString);

        assertEquals(buf, buf2);
    }

    @Test
    public void testWriteUtf8InvalidSurrogatesSwitched() {
        String surrogateString = new StringBuilder(2)
                                .append('a')
                                .append('\uDC00')
                                .append('\uD800')
                                .append('b')
                                .toString();
        ByteBuf buf = releaseLater(Unpooled.buffer(16));
        buf.writeBytes(surrogateString.getBytes(CharsetUtil.UTF_8));
        ByteBuf buf2 = releaseLater(Unpooled.buffer(16));
        ByteBufUtil.writeUtf8(buf2, surrogateString);

        assertEquals(buf, buf2);
    }

    @Test
    public void testWriteUtf8InvalidTwoLeadingSurrogates() {
        String surrogateString = new StringBuilder(2)
                                .append('a')
                                .append('\uD800')
                                .append('\uD800')
                                .append('b')
                                .toString();
        ByteBuf buf = releaseLater(Unpooled.buffer(16));
        buf.writeBytes(surrogateString.getBytes(CharsetUtil.UTF_8));
        ByteBuf buf2 = releaseLater(Unpooled.buffer(16));
        ByteBufUtil.writeUtf8(buf2, surrogateString);

        assertEquals(buf, buf2);
    }

    @Test
    public void testWriteUtf8InvalidTwoTrailingSurrogates() {
        String surrogateString = new StringBuilder(2)
                                .append('a')
                                .append('\uDC00')
                                .append('\uDC00')
                                .append('b')
                                .toString();
        ByteBuf buf = releaseLater(Unpooled.buffer(16));
        buf.writeBytes(surrogateString.getBytes(CharsetUtil.UTF_8));
        ByteBuf buf2 = releaseLater(Unpooled.buffer(16));
        ByteBufUtil.writeUtf8(buf2, surrogateString);

        assertEquals(buf, buf2);
    }

    @Test
    public void testWriteUtf8InvalidEndOnLeadingSurrogate() {
        String surrogateString = new StringBuilder(2)
                                .append('\uD800')
                                .toString();
        ByteBuf buf = releaseLater(Unpooled.buffer(16));
        buf.writeBytes(surrogateString.getBytes(CharsetUtil.UTF_8));
        ByteBuf buf2 = releaseLater(Unpooled.buffer(16));
        ByteBufUtil.writeUtf8(buf2, surrogateString);

        assertEquals(buf, buf2);
    }

    @Test
    public void testWriteUtf8InvalidEndOnTrailingSurrogate() {
        String surrogateString = new StringBuilder(2)
                                .append('\uDC00')
                                .toString();
        ByteBuf buf = releaseLater(Unpooled.buffer(16));
        buf.writeBytes(surrogateString.getBytes(CharsetUtil.UTF_8));
        ByteBuf buf2 = releaseLater(Unpooled.buffer(16));
        ByteBufUtil.writeUtf8(buf2, surrogateString);

        assertEquals(buf, buf2);
    }

    @Test
    public void testWriteUsAsciiString() {
        AsciiString usAscii = new AsciiString("NettyRocks");
        ByteBuf buf = releaseLater(Unpooled.buffer(16));
        buf.writeBytes(usAscii.toString().getBytes(CharsetUtil.US_ASCII));
        ByteBuf buf2 = releaseLater(Unpooled.buffer(16));
        ByteBufUtil.writeAscii(buf2, usAscii);

        assertEquals(buf, buf2);
    }

    @Test
    public void testWriteUtf8Wrapped() {
        String usAscii = "Some UTF-8 like ???????????";
        ByteBuf buf = unreleasableBuffer(releaseLater(Unpooled.buffer(16)));
        assertWrapped(buf);
        buf.writeBytes(usAscii.getBytes(CharsetUtil.UTF_8));
        ByteBuf buf2 = unreleasableBuffer(releaseLater(Unpooled.buffer(16)));
        assertWrapped(buf2);
        ByteBufUtil.writeUtf8(buf2, usAscii);

        assertEquals(buf, buf2);
    }

    private static void assertWrapped(ByteBuf buf) {
        assertTrue(buf instanceof WrappedByteBuf);
    }

    @Test
    public void testDecodeUsAscii() {
        testDecodeString("This is a test", CharsetUtil.US_ASCII);
    }

    @Test
    public void testDecodeUtf8() {
        testDecodeString("Some UTF-8 like ???????????", CharsetUtil.UTF_8);
    }

    private static void testDecodeString(String text, Charset charset) {
        ByteBuf buffer = Unpooled.copiedBuffer(text, charset);
        assertEquals(text, ByteBufUtil.decodeString(buffer, 0, buffer.readableBytes(), charset));
        buffer.release();
    }

    @Test
    public void testToStringDoesNotThrowIndexOutOfBounds() {
        CompositeByteBuf buffer = Unpooled.compositeBuffer();
        try {
            byte[] bytes = "1234".getBytes(CharsetUtil.UTF_8);
            buffer.addComponent(Unpooled.buffer(bytes.length).writeBytes(bytes));
            buffer.addComponent(Unpooled.buffer(bytes.length).writeBytes(bytes));
            assertEquals("1234", buffer.toString(bytes.length, bytes.length, CharsetUtil.UTF_8));
        } finally {
            buffer.release();
        }
    }
}
