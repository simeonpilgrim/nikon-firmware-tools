package com.nikonhacker.encoding;

public class FirmwareFileEntry {
        private String fileName;
        private int offset;
        private int length;
        private byte[] buffer;
        private int checkSum;

        public FirmwareFileEntry(String fileName, byte[] buffer, int offset, int length, int checkSum) {
            this.fileName = fileName;
            this.buffer = buffer;
            this.offset = offset;
            this.length = length;
            this.checkSum = checkSum;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public byte[] getBuffer() {
            return buffer;
        }

        public void setBuffer(byte[] buffer) {
            this.buffer = buffer;
        }

        public int getCheckSum() {
            return checkSum;
        }

        public void setCheckSum(int checkSum) {
            this.checkSum = checkSum;
        }

        @Override
        public String toString() {
            return "\'" + fileName + "\' from byte " + offset + " (length = " + length + ", checksum = " + checkSum + ")";
        }

}
