package Model;

public class HangarSlot {

    // ── Constants ─────────────────────────────────────────────────────────────
    public static final String STATUS_AVAILABLE   = "AVAILABLE";
    public static final String STATUS_OCCUPIED    = "OCCUPIED";
    public static final String CATEGORY_SMALL     = "SMALL";
    public static final String CATEGORY_MEDIUM    = "MEDIUM";
    public static final String CATEGORY_LARGE     = "LARGE";

    // ── Fields ────────────────────────────────────────────────────────────────
    private int    slotId;
    private String hangarName;
    private String slotCode;
    private double maxWingspan;
    private double maxLength;
    private String category;
    private String status;

    // ── Private constructor — only Builder can call this ──────────────────────
    private HangarSlot() {}

    // ── Getters ───────────────────────────────────────────────────────────────
    public int    getSlotId()     { return slotId; }
    public String getHangarName() { return hangarName; }
    public String getSlotCode()   { return slotCode; }
    public double getMaxWingspan(){ return maxWingspan; }
    public double getMaxLength()  { return maxLength; }
    public String getCategory()   { return category; }
    public String getStatus()     { return status; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setSlotId(int v)      { this.slotId = v; }
    public void setHangarName(String v){ this.hangarName = v; }
    public void setSlotCode(String v) { this.slotCode = v; }
    public void setMaxWingspan(double v){ this.maxWingspan = v; }
    public void setMaxLength(double v){ this.maxLength = v; }
    public void setCategory(String v) { this.category = v; }
    public void setStatus(String v)   { this.status = v; }

    @Override
    public String toString() {
        return String.format(
                "  ID: %-4d | Hangar: %-10s | Slot: %-4s | Category: %-7s | " +
                        "Max Wingspan: %5.1f m | Max Length: %5.1f m | [%s]",
                slotId, hangarName, slotCode, category,
                maxWingspan, maxLength, status
        );
    }

    // ════════════════════════════════════════════════════════════════════════
    // BUILDER
    // ════════════════════════════════════════════════════════════════════════

    public static class Builder {

        // ── Fields ────────────────────────────────────────────────────────────
        private int    slotId     = 0;
        private String hangarName;
        private String slotCode;
        private double maxWingspan;
        private double maxLength;
        private String category;
        private String status     = STATUS_AVAILABLE;

        // ── Setters ───────────────────────────────────────────────────────────
        public Builder slotId(int v)       { this.slotId = v;      return this; }
        public Builder hangarName(String v) { this.hangarName = v;  return this; }
        public Builder slotCode(String v)  { this.slotCode = v;    return this; }
        public Builder maxWingspan(double v){ this.maxWingspan = v; return this; }
        public Builder maxLength(double v) { this.maxLength = v;   return this; }
        public Builder category(String v)  { this.category = v;    return this; }
        public Builder status(String v)    { this.status = v;      return this; }

        // ── Build ─────────────────────────────────────────────────────────────
        public HangarSlot build() {
            HangarSlot hs   = new HangarSlot();
            hs.slotId       = this.slotId;
            hs.hangarName   = this.hangarName;
            hs.slotCode     = this.slotCode;
            hs.maxWingspan  = this.maxWingspan;
            hs.maxLength    = this.maxLength;
            hs.category     = this.category;
            hs.status       = this.status;
            return hs;
        }
    }
}