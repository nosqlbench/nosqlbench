package io.nosqlbench.engine.rest.transfertypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class WorkspaceView {

    private final static PeriodFormatter pf = new PeriodFormatterBuilder()
        .appendWeeks().appendSuffix(" week", " weeks").appendSeparator(" ")
        .appendDays().appendSuffix(" day", " days").appendSeparator(" ")
        .appendHours().appendSuffix("H")
        .appendMinutes().appendSuffix("M")
        .appendSeconds().appendSuffix("S")
        .toFormatter();

    private final Path workspaceRoot;
    private Summary summary;

    @JsonProperty("ls")
    private List<WorkspaceItemView> listing = null;

    public WorkspaceView(Path workspaceRoot) {
        this.workspaceRoot = workspaceRoot;
    }

    public String getName() {
        return workspaceRoot.getFileName().toString();
    }

    @JsonProperty("modified")
    public long getModified() {
        try {
            return Files.getLastModifiedTime(workspaceRoot).toMillis();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @JsonProperty("summary")
    public Summary getSummary() {
        if (this.summary == null) {
            Summary v = new Summary(this.workspaceRoot);
            try {
                Files.walkFileTree(this.workspaceRoot, v);
                this.summary = v;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return this.summary;
    }

    public void setListing(List<WorkspaceItemView> listing) {
        this.listing = listing;
    }

    public final static class Summary extends SimpleFileVisitor<Path> {

        private final Path root;

        public long total_bytes = 0L;
        public long total_files = 0L;
        public long last_changed_epoch = Long.MIN_VALUE;
        public String last_changed_filename = "";

        public String getLast_changed_ago() {
            int millis = (int) (System.currentTimeMillis() - last_changed_epoch);
            Period period = Period.millis(millis);
            return pf.print(period.normalizedStandard());
        }

        public Summary(Path root) {
            this.root = root;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            total_bytes += Files.size(file);
            total_files++;
            long millis= Files.getLastModifiedTime(file).toMillis();
            if (last_changed_epoch <millis) {
                last_changed_epoch = millis;
                last_changed_filename = root.relativize(file).toString();
            }
            return super.visitFile(file, attrs);
        }
    }
}
