/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.virtdata.library.xvec;

import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class IVecReaderTest {

@Test
public void testReadIvecResources() {
    final String filePath = "src/test/resources/xvec/test_ada_002_10000_indices_query_10000.ivec";

    // Get initial number of open file descriptors
    long initialFdCount = getOpenFileDescriptorCount();
    System.out.println("Initial open file descriptors: " + initialFdCount);

    final int ITERATIONS = 1000;
    final double MARGIN_PERCENT = 0.10; // 10% margin of error

    try (IVecReader reader = new IVecReader(filePath)) {
        // Make initial access to ensure first-time initialization is done
        reader.apply(0);

        // Get baseline after first access
        long baselineFdCount = getOpenFileDescriptorCount();
        System.out.println("Baseline open file descriptors after first access: " + baselineFdCount);
        
        // Calculate allowed margin based on 10% of baseline
        long allowedMargin = Math.max(3, (long)(baselineFdCount * MARGIN_PERCENT));
        System.out.println("Allowing up to " + allowedMargin + " additional file descriptors (10% margin)");

        // Make many calls and check if file descriptors are leaking
        for (int i = 0; i < ITERATIONS; i++) {
            reader.apply(i % 100); // Cycle through different indices

            if (i % 100 == 0) {
                long currentFdCount = getOpenFileDescriptorCount();
                System.out.println("Open file descriptors after " + i + " iterations: " + currentFdCount);

                // The number of open file descriptors should not increase by more than our margin
                assertThat(currentFdCount).isLessThanOrEqualTo(baselineFdCount + allowedMargin)
                    .withFailMessage("File descriptor count increased too much at iteration " + i +
                                     ": from " + baselineFdCount + " to " + currentFdCount + 
                                     " (allowed margin: " + allowedMargin + ")");
            }
        }

        // Final check
        long finalItFdCount = getOpenFileDescriptorCount();
        System.out.println("Open file descriptors after all iterations: " + finalItFdCount);
        assertThat(finalItFdCount).isLessThanOrEqualTo(baselineFdCount + allowedMargin)
            .withFailMessage("File descriptor count increased significantly after all iterations: " +
                           "baseline=" + baselineFdCount + ", final=" + finalItFdCount + 
                           " (allowed margin: " + allowedMargin + ")");
    } catch (Exception e) {
        fail("Exception while using IVecReader: " + e.getMessage());
    }

    // Force garbage collection to ensure all resources are released
    System.gc();
    System.runFinalization();

    // Calculate allowed margin for final check based on initial count
    long finalAllowedMargin = Math.max(3, (long)(initialFdCount * MARGIN_PERCENT));

    // Check that file descriptors are back to near initial count after closing
    long finalFdCount = getOpenFileDescriptorCount();
    System.out.println("Final open file descriptors after closing: " + finalFdCount);
    assertThat(finalFdCount).isLessThanOrEqualTo(initialFdCount + finalAllowedMargin)
        .withFailMessage("File descriptor count did not return to initial level: " +
                       "initial=" + initialFdCount + ", final=" + finalFdCount + 
                       " (allowed margin: " + finalAllowedMargin + ")");
}

/**
 * Gets the current number of open file descriptors for this JVM process.
 * @return The number of open file descriptors, or -1 if unable to determine
 */
private long getOpenFileDescriptorCount() {
    try {
        java.lang.management.OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        if (os instanceof com.sun.management.UnixOperatingSystemMXBean sunOs) {
            return sunOs.getOpenFileDescriptorCount();
        }

        // Fallback for platforms where com.sun.management APIs are not available
        // Note: This is much less reliable and may not give accurate results
        java.io.File procSelf = new java.io.File("/proc/self/fd");
        if (procSelf.exists() && procSelf.isDirectory()) {
            String[] fileList = procSelf.list();
            return fileList != null ? fileList.length : -1;
        }

        // Unable to determine
        return -1;
    } catch (Exception e) {
        System.err.println("Error getting open file descriptor count: " + e.getMessage());
        return -1;
    }
}
    @Test
    public void testReadIvec() {

        ArrayList<HashSet<Integer>> idx_ref = IvecFvecMethods.readIvecs("src/test/resources/xvec/test_ada_002_10000_indices_query_10000.ivec");

        IVecReader ir = new IVecReader("src/test/resources/xvec/test_ada_002_10000_indices_query_10000.ivec");
        for (int i = 0; i < 10; i++) {
            int[] indices = ir.apply(0);
            HashSet<Integer> ref = idx_ref.get(0);
            for (int j = 0; j < indices.length; j++) {
                assertThat(indices[j]).isGreaterThanOrEqualTo(0);
                assertThat(indices[j]).isLessThan(10000);
            }
        }
    }

    @Test
    public void testReadFvec() {
        FVecReader ir = new FVecReader("src/test/resources/xvec/test_ada_002_10000_distances_count"
                                       + ".fvec");
        for (int i = 0; i < 10; i++) {
            float[] dist = ir.apply(i);
            for (int j = 1; j < dist.length; j++) {
                assertThat(dist[j]).isGreaterThanOrEqualTo(dist[j-1]).describedAs("dist[" + j +"]=(" +dist[j]+") dist[j-1]=(" + dist[j-1] + ")");
            }
        }
    }

    @Test
    public void testReadFvecSpecificDims() {
        FVecReader ir = new FVecReader(
            "src/test/resources/xvec/test_ada_002_10000_base_vectors.fvec",
            1536,0);
        float[] vec0 = ir.apply(0);
        assertThat(vec0.length).isEqualTo(1536);
    }

}