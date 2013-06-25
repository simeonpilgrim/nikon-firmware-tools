package com.nikonhacker.emu.peripherials.interruptController;

import com.nikonhacker.emu.Platform;
import com.nikonhacker.emu.interrupt.InterruptRequest;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractInterruptController implements InterruptController {
    protected final List<InterruptRequest> interruptRequestQueue = new ArrayList<InterruptRequest>();

    /**
     * TODO this is weird: interruptController requires a platform
     * but platform also points to interruptController
     */
    protected Platform platform;

    public AbstractInterruptController(Platform platform) {
        this.platform = platform;
    }

    public abstract boolean request(int interruptNumber);

    /**
     * This is a way to cause completely custom (or even bogus) InterruptRequests
     * @param interruptRequest
     * @return
     */
    public abstract boolean request(InterruptRequest interruptRequest);

    /**
     * This is the way to remove a request by number
     * @param interruptNumber
     */
    @Override
    public void removeRequest(int interruptNumber) {
        synchronized (interruptRequestQueue) {
            InterruptRequest requestToRemove = null;
            for (InterruptRequest interruptRequest : interruptRequestQueue) {
                if (interruptRequest.getInterruptNumber() == interruptNumber) {
                    requestToRemove = interruptRequest;
                    break;
                }
            }
            if (requestToRemove != null) {
                interruptRequestQueue.remove(requestToRemove);
            }
        }
    }

    /**
     * This is the way to remove a specific request object
     * @param interruptRequest
     */
    public void removeRequest(InterruptRequest interruptRequest) {
        synchronized (interruptRequestQueue) {
            interruptRequestQueue.remove(interruptRequest);
        }
    }

    /**
     * Determine if there is at least one request pending
     * @return
     */
    public boolean hasPendingRequests() {
        return !interruptRequestQueue.isEmpty();
    }

    /**
     * Return the highest priority request in queue
     * @return
     */
    public InterruptRequest getNextRequest() {
        synchronized (interruptRequestQueue) {
            if (interruptRequestQueue.isEmpty()) {
                return null;
            }
            else {
                return interruptRequestQueue.get(0);
            }
        }
    }

    public List<InterruptRequest> getInterruptRequestQueue() {
        return interruptRequestQueue;
    }

}
