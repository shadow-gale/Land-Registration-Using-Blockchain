package org.landregistry;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import com.owlike.genson.Genson;

@Contract(
        name = "LandRegistryContract",
        info = @Info(
                title = "Land Registry Smart Contract",
                description = "The core chaincode for the Decentralized Land Registration System",
                version = "1.0.0",
                license = @License(name = "Apache 2.0"),
                contact = @Contact(email = "admin@landregistry.gov.in", name = "Land Registry Admin")
        )
)
@Default
public final class LandRegistryContract implements ContractInterface {

    private final Genson genson = new Genson();

    private enum LandRegistryErrors {
        ASSET_NOT_FOUND,
        ASSET_ALREADY_EXISTS,
        ASSET_NOT_ACTIVE
    }

    /**
     * Initializes the ledger with a genesis block of land records (Optional/For Testing).
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void initLedger(final Context ctx) {
        // We can leave this empty or add a dummy genesis asset here later.
        System.out.println("Land Registry Ledger Initialized.");
    }

    /**
     * Creates a new Land Asset on the blockchain.
     * * @param ctx the transaction context
     * @param ulpin the Unique Land Parcel Identification Number (Primary Key)
     * @param ownerAadhaarHash the hashed identity of the owner
     * @param gpsCoordinates the mathematical anchor (e.g., "Lat: 28.6139, Long: 77.2090")
     * @param documentCid the IPFS hash of the physical deed
     * @return the created LandAsset
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public LandAsset createLandAsset(final Context ctx, final String ulpin, final String ownerAadhaarHash, 
                                     final String gpsCoordinates, final String documentCid) {
        
        // 1. Validation: Ensure the ULPIN doesn't already exist to prevent double-registration
        if (assetExists(ctx, ulpin)) {
            throw new ChaincodeException("Land Asset with ULPIN " + ulpin + " already exists", 
                                         LandRegistryErrors.ASSET_ALREADY_EXISTS.toString());
        }

        // 2. Creation: Instantiate the immutable Java object. Root assets have a null parent.
        LandAsset land = new LandAsset(ulpin, ownerAadhaarHash, gpsCoordinates, documentCid, "ACTIVE", "NONE");

        // 3. Serialization: Convert the Java object to JSON
        String landJson = genson.serialize(land);

        // 4. Persistence: Save the JSON payload to the CouchDB world state
        ctx.getStub().putStringState(ulpin, landJson);

        return land;
    }

    /**
     * Updates the owner of an existing, active Land Asset.
     * * @param ctx the transaction context
     * @param ulpin the Primary Key of the land being transferred
     * @param newOwnerAadhaarHash the hashed identity of the new owner
     * @return the updated LandAsset
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public LandAsset transferLandOwnership(final Context ctx, final String ulpin, final String newOwnerAadhaarHash) {
        
        // 1. Validation: Ensure the asset exists
        String landJson = ctx.getStub().getStringState(ulpin);
        if (landJson == null || landJson.isEmpty()) {
            throw new ChaincodeException("Land Asset with ULPIN " + ulpin + " does not exist", 
                                         LandRegistryErrors.ASSET_NOT_FOUND.toString());
        }

        // 2. Deserialization: Convert JSON back to a Java object
        LandAsset land = genson.deserialize(landJson, LandAsset.class);

        // 3. State Check: You cannot transfer land that has been split/merged (retired)
        if (!land.getStatus().equals("ACTIVE")) {
            throw new ChaincodeException("Land Asset " + ulpin + " is not ACTIVE and cannot be transferred", 
                                         LandRegistryErrors.ASSET_NOT_ACTIVE.toString());
        }

        // 4. Update: Create a new instance with the updated owner (enforcing immutability in memory)
        LandAsset updatedLand = new LandAsset(
            land.getUlpin(), 
            newOwnerAadhaarHash, // <-- The transferred ownership
            land.getGpsCoordinates(), 
            land.getDocumentCid(), 
            land.getStatus(), 
            land.getParentUlpin()
        );

        // 5. Persistence: Overwrite the world state with the new JSON
        String updatedLandJson = genson.serialize(updatedLand);
        ctx.getStub().putStringState(ulpin, updatedLandJson);

        return updatedLand;
    }

    /**
     * Helper method to check if a land asset exists in the world state.
     */
    private boolean assetExists(final Context ctx, final String ulpin) {
        String landJson = ctx.getStub().getStringState(ulpin);
        return (landJson != null && !landJson.isEmpty());
    }
}