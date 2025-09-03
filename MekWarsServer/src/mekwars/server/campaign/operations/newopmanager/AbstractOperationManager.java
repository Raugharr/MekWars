package mekwars.server.campaign.operations.newopmanager;

import java.util.TreeMap;

import mekwars.common.campaign.operations.ModifyingOperation;
import mekwars.common.campaign.operations.Operation;
import mekwars.server.campaign.operations.LongOperation;
import mekwars.server.campaign.operations.OperationLoader;
import mekwars.server.campaign.operations.OperationWriter;
import mekwars.server.campaign.operations.OpsDisconnectionThread;
import mekwars.server.campaign.operations.OpsScrapThread;
import mekwars.server.campaign.operations.ShortOperation;
import mekwars.server.campaign.operations.ShortResolver;
import mekwars.server.campaign.operations.ShortValidator;

public abstract class AbstractOperationManager {

    //red/write classes
    protected OperationLoader opLoader;
    protected OperationWriter opWriter;
    
    //resolvers
    protected ShortResolver shortResolver;
    
    //validators
    protected ShortValidator shortValidator;
    //private LongValidator  longValidator;
    
    //local maps
    protected TreeMap<String, Operation> ops;
    protected TreeMap<String, ModifyingOperation> mods;
    
    //running operations
    protected TreeMap<Integer, ShortOperation> runningOperations;//shorts
    
    //disonnection and scrap handling
    protected TreeMap<String, OpsDisconnectionThread> disconnectionThreads;
    protected TreeMap<String, OpsScrapThread> scrapThreads; 
    
    protected TreeMap<String, Long> disconnectionTimestamps;
    protected TreeMap<String, Long> disconnectionDurations;
    
    //Map of outstanding long operations
    //ISSUE: should these be somehow sorted by faction?
    protected TreeMap<Integer, LongOperation> activeLongOps;
    
    protected boolean MULOnlyArmiesOpsLoad = false;
    
}
