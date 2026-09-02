package io.nosqlbench.virtdata.lib.vectors.vectordata;

/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.vectordata.Catalog;
import io.nosqlbench.vectordata.CatalogSources;
import io.nosqlbench.vectordata.DSWindow;
import io.nosqlbench.vectordata.FacetDescriptor;
import io.nosqlbench.vectordata.FacetNames;
import io.nosqlbench.vectordata.PrefetchHandle;
import io.nosqlbench.vectordata.TestDataView;
import io.nosqlbench.vectordata.VectorDataSettings;
import io.nosqlbench.vectordata.VectorReader;
import io.nosqlbench.vectordata.WholeFacetFallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.LongFunction;

/// Base plumbing for vectordata-backed binding functions: opens the
/// named dataset profile from the default catalogs and warms an
/// optional record window according to a prefetch mode:
///
/// - `eager` (aliases `prebuffer`, `true`) — fetch the effective window,
///   or the whole facet when no window applies, before returning;
/// - `background` — start the same fetch on another thread and return
///   immediately, letting reads overlap the download;
/// - `none` (aliases `demand`, `false`) — demand-paged access only.
///
/// A binding window selects **which records to warm**, and nothing
/// else: indices stay absolute record ordinals, so a workload running
/// `cycles=50000..100000` with a window of `[50000..100000)` addresses
/// the records those cycles name. Reads outside the window are not an
/// error — they simply demand-page, the way they would with no window
/// at all — which also keeps the binding resolvable, since VirtData
/// probes a newly resolved function with a small sample index before
/// any cycle runs. Ordinals mean the same thing here, in
/// [VariableFacet], and in the `prefetchCycles` expression function.
/// The window is translated into dataset coordinates against any
/// profile-declared window, so the bytes fetched are the bytes the
/// reader will expose.
public abstract class CoreVectors<T> implements LongFunction<T> {

    protected final TestDataView tdv;
    protected final String facetName;
    protected final VectorReader<T> dataset;
    private final PrefetchHandle backgroundPrefetch;

    protected CoreVectors(String datasetAndProfile, String facetName, boolean prebuffer, VectorDataSettings settings) {
        this(datasetAndProfile, facetName, "", prebuffer ? "eager" : "none", settings);
    }

    protected CoreVectors(String datasetAndProfile, String facetName, String window, String prefetchMode,
                          VectorDataSettings settings) {
        Catalog catalog = Catalog.of(CatalogSources.defaults(), settings);
        tdv = catalog.openProfile(datasetAndProfile);
        this.facetName = FacetNames.canonical(facetName);
        // Deliberately unwrapped: the window warms, it does not clip.
        dataset = getRandomAccessData();
        backgroundPrefetch = warm(window, Prefetch.parse(prefetchMode));
    }

    protected abstract VectorReader<T> getRandomAccessData();

    /// The handle for a `background` prefetch, or `null` in the other
    /// modes.
    public PrefetchHandle backgroundPrefetch() { return backgroundPrefetch; }

    private PrefetchHandle warm(String window, Prefetch mode) {
        if (mode == Prefetch.NONE) return null;
        DSWindow effective = effectiveWindow(tdv, facetName, window);
        if (effective == null) return null;
        // The plan is announced before any bytes move and progress is
        // emitted during the download; the meter is silent when there
        // is nothing to fetch.
        PrefetchMeter meter = new PrefetchMeter(tdv.dataset() + ":" + tdv.profile() + ":" + facetName, tdv.prefetchPlan(facetName, effective));
        if (mode == Prefetch.BACKGROUND) {
            PrefetchHandle handle = tdv.prefetchInBackground(facetName, effective, WholeFacetFallback.REFUSE);
            meter.watch(handle);
            return handle;
        }
        tdv.prefetch(facetName, effective, WholeFacetFallback.REFUSE, meter);
        meter.complete();
        return null;
    }

    /// Translates a binding window into dataset record coordinates for
    /// the fetch. A profile-declared window re-bases the reader it
    /// hands back, so a binding index is offset by that window's start
    /// and clamped to its end; profiles in practice declare windows
    /// starting at 0, where this is the identity. Returns the profile
    /// window itself when no binding window is given, [DSWindow#ALL]
    /// when neither applies, and `null` when the binding window falls
    /// entirely outside the profile window — nothing to warm.
    static DSWindow effectiveWindow(TestDataView tdv, String facetName, String window) {
        DSWindow binding = DSWindow.parse(window == null ? "" : window);
        DSWindow profile = tdv.facet(facetName)
            .map(FacetDescriptor::window)
            .filter(declared -> declared != null && !declared.isBlank())
            .map(DSWindow::parse)
            .orElse(DSWindow.ALL);
        if (binding.isEmpty()) return profile;
        if (profile.isEmpty()) return binding;
        DSWindow.Interval base = profile.intervals().get(0);
        List<DSWindow.Interval> shifted = new ArrayList<>();
        for (DSWindow.Interval interval : binding.intervals()) {
            long start = saturatedAdd(base.minIncl(), interval.minIncl());
            long end = Math.min(saturatedAdd(base.minIncl(), interval.maxExcl()), base.maxExcl());
            if (end > start) shifted.add(new DSWindow.Interval(start, end));
        }
        return shifted.isEmpty() ? null : new DSWindow(shifted);
    }

    private static long saturatedAdd(long a, long b) {
        try { return Math.addExact(a, b); } catch (ArithmeticException overflow) { return Long.MAX_VALUE; }
    }

    /// How a binding warms its data before first read.
    enum Prefetch {
        EAGER, BACKGROUND, NONE;

        static Prefetch parse(String mode) {
            return switch (mode == null ? "" : mode.toLowerCase(Locale.ROOT)) {
                case "eager", "prebuffer", "true" -> EAGER;
                case "background" -> BACKGROUND;
                case "none", "demand", "false" -> NONE;
                default -> throw new RuntimeException(
                    "Unknown prefetch mode '" + mode + "': expected eager, background, or none");
            };
        }
    }

    @Override
    public T apply(long value) {
        return dataset.get(value);
    }

}
