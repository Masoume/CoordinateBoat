package sami.uilanguage.fromui;

import sami.allocation.ResourceAllocation;
import java.util.UUID;

/**
 *
 * @author nbb
 */
public class AllocationSelectedMessage extends FromUiMessage {

    private ResourceAllocation allocation;

    public AllocationSelectedMessage(UUID relevantToUiMessageId, UUID relevantOutputEventId, UUID missionId, ResourceAllocation allocation) {
        super(relevantToUiMessageId, relevantOutputEventId, missionId);
        this.allocation = allocation;
    }

    public ResourceAllocation getAllocation() {
        return allocation;
    }

    public void setAllocation(ResourceAllocation allocation) {
        this.allocation = allocation;
    }

    public String toString() {
        return "AllocationSelectedMessage [" + allocation + "]";
    }
}
