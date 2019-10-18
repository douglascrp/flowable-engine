/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.common.engine.impl.eventregistry.consumer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.flowable.common.engine.api.eventbus.FlowableEventBusEvent;
import org.flowable.common.engine.api.eventregistry.EventRegistry;
import org.flowable.common.engine.api.eventregistry.EventRegistryEventBusConsumer;
import org.flowable.common.engine.api.eventregistry.runtime.EventCorrelationParameterInstance;
import org.flowable.common.engine.api.eventregistry.runtime.EventInstance;
import org.flowable.common.engine.impl.eventregistry.event.EventRegistryEvent;

/**
 * @author Joram Barrez
 * @author Filip Hrisafov
 */
public abstract class BaseEventRegistryEventConsumer implements EventRegistryEventBusConsumer {

    protected Collection<String> supportedTypes = new LinkedHashSet<>();
    protected EventRegistry eventRegistry;

    public BaseEventRegistryEventConsumer(EventRegistry eventRegistry) {
        this.eventRegistry = eventRegistry;
    }

    public void addSupportedType(String supportedType) {
        if (!supportedTypes.contains(supportedType)) {
            supportedTypes.add(supportedType);
        }
    }

    @Override
    public Collection<String> getSupportedTypes() {
        return supportedTypes;
    }

    @Override
    public void eventReceived(FlowableEventBusEvent event) {
        EventRegistryEvent eventRegistryEvent = (EventRegistryEvent) event;
        if (eventRegistryEvent.getEventInstance() != null) {
            eventReceived(eventRegistryEvent.getEventInstance());
        } else {
            // TODO: what should happen in this case?
        }
    }

    protected abstract void eventReceived(EventInstance eventInstance);

    protected Collection<String> generateCorrelationKeys(Collection<EventCorrelationParameterInstance> correlationParameterInstances) {
        if (correlationParameterInstances.isEmpty()) {
            return Collections.emptySet();
        }
        List<EventCorrelationParameterInstance> list = new ArrayList<>(correlationParameterInstances);
        Collection<String> correlationKeys = new HashSet<>();
        for (int i = 1; i <= list.size(); i++) {
            for (int j = 0; j <= list.size() - i; j++) {
                correlationKeys.add(generateCorrelationKey(list.subList(j, j + i)));
            }
        }

        return correlationKeys;
    }

    protected String generateCorrelationKey(Collection<EventCorrelationParameterInstance> correlationParameterInstances) {
        Map<String, Object> data = new HashMap<>();
        for (EventCorrelationParameterInstance correlationParameterInstance : correlationParameterInstances) {
            data.put(correlationParameterInstance.getDefinitionName(), correlationParameterInstance.getValue());
        }

        return eventRegistry.generateKey(data);
    }

}
