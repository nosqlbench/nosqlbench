package io.virtdata.libbasics.core.stathelpers;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.DoubleToIntFunction;
import java.util.stream.Collectors;

/**
 * Uses the alias sampling method to encode and sample from discrete probabilities,
 * even over larger sets of data. This form requires a unit interval sample value
 * between 0.0 and 1.0. Assuming the maximal amount of memory is used for distinct
 * outcomes N, a memory buffer of N*16 bytes is required for this implementation,
 * requiring 32MB of memory for 1M entries.
 *
 * This sampler should be shared between threads, and will be by default, in order
 * to avoid many instances of a 32MB buffer on heap.
 */
public class AliasSamplerDoubleInt implements DoubleToIntFunction {

    private ByteBuffer stats; // tuples of double,int,int (unfair coin, direct pointers to referents)
    private double slotCount; // The number of fair die-roll slotCount that contain unfair coin probabilities
    private static int _r0=0;
    private static int _r1=_r0+Double.BYTES;
    private static int _r2=_r1+Integer.BYTES;
    public static int RECORD_LEN = _r2 + Integer.BYTES; // Record size for the above.

    // for testing
    AliasSamplerDoubleInt(ByteBuffer stats) {
        this.stats = stats;
        if ((stats.capacity()% RECORD_LEN)!=0) {
            throw new RuntimeException("Misaligned ByteBuffer size, must be a multiple of " + RECORD_LEN);
        }
        slotCount = (stats.capacity()/ RECORD_LEN);
    }

    public AliasSamplerDoubleInt(List<EvProbD> events) {
        int size = events.size();

        int[] alias = new int[events.size()];
        double[] prob = new double[events.size()];

        LinkedList<EvProbD> small = new LinkedList<>();
        LinkedList<EvProbD> large = new LinkedList<>();
        List<Slot> slots = new ArrayList<>();

        // array-size normalization
        double sumProbability = events.stream().mapToDouble(EvProbD::getProbability).sum();
        events = events.stream().map(e -> new EvProbD(e.getEventId(), (e.getProbability()/sumProbability)*size)).collect(Collectors.toList());

        // presort
        for (EvProbD event : events) {
            (event.getProbability()<1.0D ? small : large).addLast(event);
        }

        while (small.peekFirst()!=null && large.peekFirst()!=null) {
            EvProbD l = small.removeFirst();
            EvProbD g = large.removeFirst();
            slots.add(new Slot(g.getEventId(), l.getEventId(), l.getProbability()));
            g.setProbability((g.getProbability()+l.getProbability())-1);
            (g.getProbability()<1.0D ? small : large).addLast(g); // requeue
        }
        while (large.peekFirst()!=null) {
            EvProbD g = large.removeFirst();
            slots.add(new Slot(g.getEventId(),g.getEventId(),1.0));
        }
        while (small.peekFirst()!=null) {
            EvProbD l = small.removeFirst();
            slots.add(new Slot(l.getEventId(),l.getEventId(),1.0));
        }
        if (slots.size()!=size) {
            throw new RuntimeException("basis for average probability is incorrect, because only " + slots.size() + " slotCount of " + size + " were created.");
        }
        // align to indexes
        for (int i = 0; i < slots.size(); i++) {
            slots.get(i).rescale(i, i+1);
        }
        this.stats = ByteBuffer.allocate(slots.size()* RECORD_LEN);

        for (Slot slot : slots) {
            stats.putDouble(slot.botProb);
            stats.putInt(slot.botItx);
            stats.putInt(slot.topIdx);
        }
        stats.flip();
        this.slotCount = (stats.capacity()/ RECORD_LEN);

    }

    @Override
    public int applyAsInt(double value) {
        double fractionlPoint = value * slotCount;
        int offsetPoint = (int) fractionlPoint * RECORD_LEN;
        double divider = stats.getDouble(offsetPoint);
        int selector = offsetPoint+ (fractionlPoint>divider?_r2:_r1);
        int referentId = stats.getInt(selector);
        return referentId;
    }

    private static class Slot {
        public int topIdx;
        public int botItx;
        public double botProb;

        public Slot(int topIdx, int botItx, double botProb) {
            this.topIdx = topIdx;
            this.botItx = botItx;
            this.botProb = botProb;
        }

        public String toString() {
            return "top:" + topIdx + ", bot:" + botItx + ", botProb: " + botProb;
        }

        public Slot rescale(double min, double max) {
            botProb = (min + (botProb*(max-min)));
            return this;
        }
    }
}
