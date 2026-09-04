/*
 * Copyright (c) 2026 The NoSQLBench Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.nosqlbench.vectordata.internal;

import io.nosqlbench.vectordata.VectorDataException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/// A slab's page index — which data page holds each ordinal, and where
/// that page starts in the file — read from the file's tail.
///
/// A slab is a sequence of pages, each carrying a 16-byte footer with
/// its first ordinal and record count, and ends with a *pages page*
/// indexing every data page by start ordinal. A file with several
/// namespaces ends instead with a *namespaces page* locating each
/// namespace's pages page. Reading the index is therefore a bounded
/// read of the footer and one or two short pages — never a walk of the
/// file — which is what keeps planning cheap: it must not move the
/// bytes it is deciding whether to move. The count of the last page is
/// the one figure the index does not carry, so that page is read too.
///
/// All multi-byte integers are little-endian; file offsets and ordinals
/// in index entries are signed 64-bit, the footer's ordinal is 5 bytes
/// sign-extended and its record count 3 bytes.
public final class SlabIndex {
    static final int FOOTER = 16;
    static final int HEADER = 8;
    static final int PAGE_TYPE_PAGES = 1;
    static final int PAGE_TYPE_DATA = 2;
    static final int PAGE_TYPE_NAMESPACES = 3;
    static final byte[] MAGIC = {'S', 'L', 'A', 'B'};

    /// One data page: its first ordinal, and its byte offset in the file.
    public record PageEntry(long startOrdinal, long fileOffset) { }

    private final List<PageEntry> entries;
    private final long total;
    private final long prerequisiteBytes;

    private SlabIndex(List<PageEntry> entries, long total, long prerequisiteBytes) {
        this.entries = List.copyOf(entries); this.total = total; this.prerequisiteBytes = prerequisiteBytes;
    }

    /// Records in the namespace this index covers.
    public long total() { return total; }

    /// Bytes read to build this index: the footer, the tail page, and
    /// the pages page a namespaces page pointed at.
    public long prerequisiteBytes() { return prerequisiteBytes; }

    public int pageCount() { return entries.size(); }

    /// The page holding `ordinal`, or `null` past the end — never a
    /// clamp, because a silently clamped ordinal reads the wrong page.
    public Integer pageOf(long ordinal) {
        if (ordinal < 0 || ordinal >= total) return null;
        int lo = 0, hi = entries.size() - 1, found = -1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (entries.get(mid).startOrdinal() <= ordinal) { found = mid; lo = mid + 1; } else hi = mid - 1;
        }
        return found < 0 ? null : found;
    }

    /// Byte offset of a page, or `null` for a page that does not exist.
    public Long pageOffset(int page) { return page >= 0 && page < entries.size() ? entries.get(page).fileOffset() : null; }

    /// First ordinal of a page, or `null` for a page that does not exist.
    public Long pageStartOrdinal(int page) { return page >= 0 && page < entries.size() ? entries.get(page).startOrdinal() : null; }

    /// Reads the index of `namespace` (`null` or empty for the default)
    /// from a slab's tail. `null` when the file has no such namespace —
    /// a normal state, not a fault. A file that is not a slab, or a
    /// slab whose tail does not parse, fails naming `label`.
    public static SlabIndex read(ByteStorage storage, String namespace, String label) {
        long fileLen = storage.size();
        if (fileLen < FOOTER) throw fail(label, "slab", "too small (" + fileLen + " bytes)");
        long prerequisite = FOOTER;
        Footer footer = Footer.parse(read(storage, fileLen - FOOTER, FOOTER, label), label, "footer");
        if (footer.pageSize() > fileLen) throw fail(label, "footer", "page size " + footer.pageSize() + " exceeds the file");
        byte[] tail = read(storage, fileLen - footer.pageSize(), footer.pageSize(), label);
        prerequisite += footer.pageSize();
        List<PageEntry> entries;
        if (footer.pageType() == PAGE_TYPE_PAGES) {
            if (namespace != null && !namespace.isEmpty()) return null;
            entries = pagesPage(tail, label);
        } else {
            Page namespaces = Page.parse(tail, label, "namespaces page");
            if (namespaces.footer().pageType() != PAGE_TYPE_NAMESPACES)
                throw fail(label, "namespaces page", "invalid page type " + namespaces.footer().pageType());
            String wanted = namespace == null ? "" : namespace;
            Long pagesAt = null;
            for (byte[] record : namespaces.records()) {
                NamespaceEntry entry = NamespaceEntry.parse(record, label);
                if (entry.name().equals(wanted)) { pagesAt = entry.pagesPageOffset(); break; }
            }
            if (pagesAt == null) return null;
            byte[] bytes = pageAt(storage, pagesAt, fileLen, label);
            prerequisite += bytes.length;
            entries = pagesPage(bytes, label);
        }
        entries.sort(Comparator.comparingLong(PageEntry::startOrdinal));
        long total = 0;
        if (!entries.isEmpty()) {
            PageEntry last = entries.get(entries.size() - 1);
            byte[] bytes = pageAt(storage, last.fileOffset(), fileLen, label);
            total = last.startOrdinal() + recordCountFromBuf(bytes, label);
        }
        return new SlabIndex(entries, total, prerequisite);
    }

    /// The size a page declares in its header, or `null` when the
    /// header cannot be read.
    static Long pageSizeAt(ByteStorage storage, long offset) {
        try {
            if (offset < 0 || offset + HEADER > storage.size()) return null;
            return Integer.toUnsignedLong(storage.read(offset + 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt());
        } catch (VectorDataException unreadable) { return null; }
    }

    private static byte[] pageAt(ByteStorage storage, long offset, long fileLen, String label) {
        if (offset < 0 || offset + HEADER > fileLen) throw fail(label, "page header", "offset " + offset + " is outside the file");
        long size = Integer.toUnsignedLong(ByteBuffer.wrap(read(storage, offset + 4, 4, label)).order(ByteOrder.LITTLE_ENDIAN).getInt());
        if (size < HEADER + FOOTER || offset + size > fileLen) throw fail(label, "page header", "page size " + size + " at " + offset + " is not within the file");
        return read(storage, offset, size, label);
    }

    private static byte[] read(ByteStorage storage, long offset, long length, String label) {
        if (length > Integer.MAX_VALUE) throw fail(label, "read " + length + " bytes at " + offset, "page too large to buffer");
        try {
            ByteBuffer buffer = storage.read(offset, (int) length);
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return bytes;
        } catch (VectorDataException | IndexOutOfBoundsException failure) {
            throw fail(label, "read " + length + " bytes at " + offset, failure.getMessage());
        }
    }

    private static List<PageEntry> pagesPage(byte[] bytes, String label) {
        Page page = Page.parse(bytes, label, "pages page");
        if (page.footer().pageType() != PAGE_TYPE_PAGES) throw fail(label, "pages page", "invalid page type " + page.footer().pageType());
        List<PageEntry> entries = new ArrayList<>(page.records().size());
        for (byte[] record : page.records()) {
            if (record.length != 16) throw fail(label, "pages page", "index entry of " + record.length + " bytes, expected 16");
            ByteBuffer entry = ByteBuffer.wrap(record).order(ByteOrder.LITTLE_ENDIAN);
            entries.add(new PageEntry(entry.getLong(), entry.getLong()));
        }
        return entries;
    }

    /// The record count in a page's footer — the only place a data
    /// page's extent in ordinals is written.
    static long recordCountFromBuf(byte[] page, String label) {
        if (page.length < HEADER + FOOTER) throw fail(label, "last page", "truncated page of " + page.length + " bytes");
        return Footer.parse(java.util.Arrays.copyOfRange(page, page.length - FOOTER, page.length), label, "last page").recordCount();
    }

    private static VectorDataException fail(String label, String what, String detail) {
        return new VectorDataException(label + ": " + what + ": " + detail);
    }

    /// The 16-byte page footer: ordinal (5 bytes, sign-extended), record
    /// count (3 bytes), page size, page type, namespace index, and the
    /// footer's own length.
    record Footer(long startOrdinal, long recordCount, long pageSize, int pageType, int namespaceIndex, int footerLength) {
        static Footer parse(byte[] buf, String label, String what) {
            if (buf.length < FOOTER) throw fail(label, what, "buffer too small: " + buf.length + " < " + FOOTER);
            long ordinal = 0;
            for (int i = 0; i < 5; i++) ordinal |= (buf[i] & 0xffL) << (8 * i);
            if ((buf[4] & 0x80) != 0) ordinal |= 0xffffff0000000000L;
            long count = (buf[5] & 0xffL) | (buf[6] & 0xffL) << 8 | (buf[7] & 0xffL) << 16;
            long size = Integer.toUnsignedLong(ByteBuffer.wrap(buf, 8, 4).order(ByteOrder.LITTLE_ENDIAN).getInt());
            int type = buf[12] & 0xff;
            if (type < PAGE_TYPE_PAGES || type > PAGE_TYPE_NAMESPACES) throw fail(label, what, "invalid page type " + type);
            int namespaceIndex = buf[13] & 0xff;
            if (namespaceIndex == 0 || namespaceIndex >= 128) throw fail(label, what, "invalid namespace index " + namespaceIndex);
            int footerLength = Short.toUnsignedInt(ByteBuffer.wrap(buf, 14, 2).order(ByteOrder.LITTLE_ENDIAN).getShort());
            if (footerLength < FOOTER) throw fail(label, what, "footer_length " + footerLength + " < " + FOOTER);
            return new Footer(ordinal, count, size, type, namespaceIndex, footerLength);
        }
    }

    /// A parsed page: magic, size, records addressed through the offset
    /// array that sits just before the footer.
    record Page(Footer footer, List<byte[]> records) {
        static Page parse(byte[] buf, String label, String what) {
            if (buf.length < HEADER + FOOTER) throw fail(label, what, "truncated page of " + buf.length + " bytes");
            for (int i = 0; i < 4; i++) if (buf[i] != MAGIC[i]) throw fail(label, what, "invalid magic");
            long headerSize = Integer.toUnsignedLong(ByteBuffer.wrap(buf, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt());
            Footer footer = Footer.parse(java.util.Arrays.copyOfRange(buf, buf.length - FOOTER, buf.length), label, what);
            if (headerSize != footer.pageSize())
                throw fail(label, what, "page size mismatch: header " + headerSize + ", footer " + footer.pageSize());
            int count = (int) footer.recordCount();
            long offsetsAt = buf.length - FOOTER - 4L * (count + 1);
            if (offsetsAt < HEADER) throw fail(label, what, "offset array does not fit " + count + " records");
            ByteBuffer offsets = ByteBuffer.wrap(buf, (int) offsetsAt, 4 * (count + 1)).order(ByteOrder.LITTLE_ENDIAN);
            long previous = Integer.toUnsignedLong(offsets.getInt());
            List<byte[]> records = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                long next = Integer.toUnsignedLong(offsets.getInt());
                if (previous < HEADER || next < previous || next > offsetsAt) throw fail(label, what, "record " + i + " offsets are out of range");
                records.add(java.util.Arrays.copyOfRange(buf, (int) previous, (int) next));
                previous = next;
            }
            return new Page(footer, records);
        }
    }

    /// One namespaces-page record: index, name, and where that
    /// namespace's pages page starts.
    record NamespaceEntry(int namespaceIndex, String name, long pagesPageOffset) {
        static NamespaceEntry parse(byte[] buf, String label) {
            if (buf.length < 10) throw fail(label, "namespace entries", "namespace entry too short");
            int nameLength = buf[1] & 0xff;
            if (buf.length < 2 + nameLength + 8) throw fail(label, "namespace entries", "namespace entry truncated");
            String name = new String(buf, 2, nameLength, StandardCharsets.UTF_8);
            long offset = ByteBuffer.wrap(buf, 2 + nameLength, 8).order(ByteOrder.LITTLE_ENDIAN).getLong();
            return new NamespaceEntry(buf[0] & 0xff, name, offset);
        }
    }
}
