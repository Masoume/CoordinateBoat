package sami.service.information;

import sami.event.GeneratedInputEventSubscription;

/**
 *
 * @author pscerri
 */
public interface InformationServiceProviderInt {

    public boolean offer(GeneratedInputEventSubscription sub);
    public boolean cancel(GeneratedInputEventSubscription sub);
}
