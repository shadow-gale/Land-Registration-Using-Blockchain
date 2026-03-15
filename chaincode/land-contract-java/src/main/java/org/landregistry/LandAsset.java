package org.landregistry;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import com.owlike.genson.annotation.JsonProperty;
import java.util.Objects;

@DataType()
public final class LandAsset {

    @Property()
    private final String ulpin; // Primary Key: Unique Land Parcel Identification Number

    @Property()
    private final String ownerAadhaarHash; // Hashed identity for privacy

    @Property()
    private final String gpsCoordinates; // Mathematical anchor resolving the oracle problem

    @Property()
    private final String documentCid; // IPFS CID / SHA-256 Hash of the actual PDF deed

    @Property()
    private final String status; // e.g., "ACTIVE", "RETIRED_MUTATED"

    @Property()
    private final String parentUlpin; // Lineage tracking for mutations (null if root asset)

    public LandAsset(@JsonProperty("ulpin") final String ulpin,
                     @JsonProperty("ownerAadhaarHash") final String ownerAadhaarHash,
                     @JsonProperty("gpsCoordinates") final String gpsCoordinates,
                     @JsonProperty("documentCid") final String documentCid,
                     @JsonProperty("status") final String status,
                     @JsonProperty("parentUlpin") final String parentUlpin) {
        this.ulpin = ulpin;
        this.ownerAadhaarHash = ownerAadhaarHash;
        this.gpsCoordinates = gpsCoordinates;
        this.documentCid = documentCid;
        this.status = status;
        this.parentUlpin = parentUlpin;
    }

    // --- Getters ---
    public String getUlpin() { return ulpin; }
    public String getOwnerAadhaarHash() { return ownerAadhaarHash; }
    public String getGpsCoordinates() { return gpsCoordinates; }
    public String getDocumentCid() { return documentCid; }
    public String getStatus() { return status; }
    public String getParentUlpin() { return parentUlpin; }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if ((obj == null) || (getClass() != obj.getClass())) return false;
        LandAsset other = (LandAsset) obj;
        return Objects.equals(getUlpin(), other.getUlpin())
                && Objects.equals(getOwnerAadhaarHash(), other.getOwnerAadhaarHash())
                && Objects.equals(getGpsCoordinates(), other.getGpsCoordinates())
                && Objects.equals(getDocumentCid(), other.getDocumentCid())
                && Objects.equals(getStatus(), other.getStatus())
                && Objects.equals(getParentUlpin(), other.getParentUlpin());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUlpin(), getOwnerAadhaarHash(), getGpsCoordinates(), getDocumentCid(), getStatus(), getParentUlpin());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) 
            + " [ulpin=" + ulpin + ", ownerAadhaarHash=" + ownerAadhaarHash + ", status=" + status + "]";
    }
}