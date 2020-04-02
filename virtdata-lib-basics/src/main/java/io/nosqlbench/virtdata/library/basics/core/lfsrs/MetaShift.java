package io.nosqlbench.virtdata.library.basics.core.lfsrs;

import io.nosqlbench.nb.api.pathutil.NBPaths;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.function.LongUnaryOperator;

public class MetaShift {
    private final static Logger logger  = LogManager.getLogger(MetaShift.class);
    public static Func forSizeAndBank(long size, int selector) {
        GaloisData data = Masks.forPeriodAndBank(size, selector);
        return new Func(data);
    }

    public static Func forSizeAndModulo(long size, int modulo) {
        GaloisData data =  Masks.forPeriodAndBankModulo(size, modulo);
        return new Func(data);
    }

    public static long maskForMsb(long period) {
        int lsbwidth = getMsbPosition(period); // -1 due to 0x01 initializer
        long register = 1L;
        for (int i = 0; i < lsbwidth-1; i++) {
            register|=(register<<1);
        }
        return register;
    }

    private static int[] msbs = {0,1,2,2,3,3,3,3,4,4,4,4,4,4,4,4};
    public static int getMsbPosition(long value) {
        if (value<0) {
            throw new RuntimeException("Only values between 1 and " + Long.MAX_VALUE + " are supported, and you tried to get the MSB position for value " + value + " or possible overflowed to a negative value.");
        }
        int r=0;
        if ((value & 0xFFFFFFFF00000000L)>0) { r+=32 ; value >>=32; }
        if ((value & 0x00000000FFFF0000L)>0) { r+=16; value >>=16; }
        if ((value & 0x000000000000FF00L)>0) { r+=8; value >>=8; }
        if ((value & 0x00000000000000F0)>0) { r+=4; value >>=4; }
        return r + msbs[(int)value];
    }

    public static String toBitString(int value) {
        return toBitString(value,32);
    }

    public static String toBitString(long value) {
        return toBitString(value,64);
    }

    public static String toBitString(long value, long len) {
        return String.format("%" + len + "s", Long.toBinaryString(value)).replace(' ', '0');
    }

    public static String toBitString(int value, int len) {
        return String.format("%" + len + "s", Integer.toBinaryString(value)).replace(' ', '0');
    }

    public static class GaloisData {
        public final long feedback;
        public final long resamplePeriod;
        public final long width;
        public final long mask;
        public final int actualPeriod;

        public GaloisData(long feedback, long resamplePeriod, long width, long mask) {
            this.feedback = feedback;
            this.resamplePeriod = resamplePeriod;
            this.width = width;
            this.mask = mask;
            this.actualPeriod = (1<<(width))-1;
        }

        public GaloisData withOffset(long offset) {
            return new GaloisData(feedback, resamplePeriod,width,mask);
        }

        public String toString() {
            return "(feedback,resample,width,mask=("+ feedback +","+ resamplePeriod +","+width+","+mask+")";
        }

    }

    /**
     * This Shifty Imp can provide some data for you. It offsets by 1
     */
    public static class Func implements LongUnaryOperator {

        public final GaloisData config;
        public final long feedback;
        public final long periodModulo;

        public Func(GaloisData config) {
            this.config = config;
            this.feedback = config.feedback;
            this.periodModulo = config.actualPeriod;
        }

        @Override
        public long applyAsLong(long register) {
            long lsb = (register) & 1;
            register >>= 1;
            register ^= (-lsb) & feedback;
            return register;
        }

        public String toString() {
            return config.toString();
        }
    }

    public static class Masks {

        public static GaloisData forPeriodAndBank(long period, int bank) {
            int registerWidth = getMsbPosition(period);
            long[] longs = Masks.masksForBitWidth(registerWidth);
            try {
                return new GaloisData(longs[bank],period,registerWidth,MetaShift.maskForMsb(period));
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new RuntimeException("There are only " + longs.length + " items available for a cycle resamplePeriod of " + period);
            }
        }


        /**
         * Look up the valid Galois LFSR feedback register values for the required resamplePeriod.
         * @param period The minimum cycle resamplePeriod required
         * @param bankModulo A deterministic selector which will always select the same pseudo random bank
         * @return A valid Galois LFSR feedback register
         */
        public static GaloisData forPeriodAndBankModulo(long period, int bankModulo) {
            int registerWidth = getMsbPosition(period);
            long[] longs = Masks.masksForBitWidth(registerWidth);
            return new GaloisData(longs[bankModulo % longs.length],period,registerWidth,MetaShift.maskForMsb(period));
        }

        public static long[] masksForBitWidth(int registerSize) {
            if (registerSize>64) {
                throw new RuntimeException("Unable to map LFSR coefficients for registers > 64 bits wide.");
            }
            int availableSize= Math.max(registerSize,4);
            String maskFileName= String.valueOf(availableSize)+"."+"txt";
            List<String> lines = NBPaths.readDataFileLines("lfsrmasks/" + maskFileName);
            long[] longs = lines.stream().mapToLong(s -> Long.parseLong(s, 16)).toArray();
            return longs;
        }

    }
}
