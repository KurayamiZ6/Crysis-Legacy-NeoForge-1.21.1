package io.github.painworks.crysismod.client.nanogeo;

import java.util.Arrays;
import java.util.Locale;

public final class NanoGeoPerfStats {
   private static final int MAX_SAMPLES = 512;
   private long samples;
   private long totalNs;
   private long minNs = Long.MAX_VALUE;
   private long maxNs = Long.MIN_VALUE;
   private final long[] recentNs = new long[512];
   private int recentIndex;
   private int recentCount;

   public void addSample(long valueNs) {
      this.samples++;
      this.totalNs += valueNs;
      if (valueNs < this.minNs) {
         this.minNs = valueNs;
      }

      if (valueNs > this.maxNs) {
         this.maxNs = valueNs;
      }

      this.recentNs[this.recentIndex] = valueNs;
      this.recentIndex = (this.recentIndex + 1) % 512;
      if (this.recentCount < 512) {
         this.recentCount++;
      }
   }

   public long samples() {
      return this.samples;
   }

   public double avgMs() {
      return this.samples == 0L ? 0.0 : this.nanosToMs((double)this.totalNs / this.samples);
   }

   public double minMs() {
      return this.samples == 0L ? 0.0 : this.nanosToMs(this.minNs);
   }

   public double maxMs() {
      return this.samples == 0L ? 0.0 : this.nanosToMs(this.maxNs);
   }

   public double p95Ms() {
      if (this.recentCount == 0) {
         return 0.0;
      } else {
         long[] copy = Arrays.copyOf(this.recentNs, this.recentCount);
         Arrays.sort(copy);
         int p95Index = Math.max(0, (int)Math.ceil(this.recentCount * 0.95) - 1);
         return this.nanosToMs(copy[p95Index]);
      }
   }

   public String formatMsLine() {
      return "avg="
         + this.format(this.avgMs())
         + " min="
         + this.format(this.minMs())
         + " max="
         + this.format(this.maxMs())
         + " p95="
         + this.format(this.p95Ms())
         + " samples="
         + this.samples;
   }

   private double nanosToMs(double nanos) {
      return nanos / 1000000.0;
   }

   private String format(double value) {
      return String.format(Locale.ROOT, "%.4f", value);
   }
}
