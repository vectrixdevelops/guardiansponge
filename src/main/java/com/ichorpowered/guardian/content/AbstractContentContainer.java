package com.ichorpowered.guardian.content;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ichorpowered.guardian.content.transaction.GuardianBatchValue;
import com.ichorpowered.guardian.content.transaction.GuardianSingleValue;
import com.ichorpowered.guardianapi.content.ContentLoader;
import com.ichorpowered.guardianapi.content.ContentContainer;
import com.ichorpowered.guardianapi.content.transaction.ContentKey;
import com.ichorpowered.guardianapi.content.transaction.result.BatchValue;
import com.ichorpowered.guardianapi.content.transaction.result.SingleValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractContentContainer implements ContentContainer {

    private final ContentLoader contentLoader;
    private final Set<ContentKey> keySet = Sets.newHashSet();
    private final Map<String, Object> container = Maps.newHashMap();

    public AbstractContentContainer() {
        this(null);
    }

    public AbstractContentContainer(final ContentLoader contentLoader) {
        this.contentLoader = contentLoader;
    }

    @Override
    public <E> SingleValue<E> offer(ContentKey key, E value) {
        GuardianSingleValue<E> singleValueResult = new GuardianSingleValue<>(key, this);

        if (!this.keySet.contains(key)) this.keySet.add(key);
        if (!this.container.containsKey(key.getId())) this.container.put(key.getId(), singleValueResult.setElement(value).setDirty(true));

        return singleValueResult;
    }

    @Override
    public <E> Optional<SingleValue<E>> offer(String id, E value) {
        Optional<ContentKey> contentKey = this.getPossibleKeys().stream()
                .filter(key -> key.getId().equals(id))
                .findFirst();

        return contentKey.map(identifiedKey -> this.offer(identifiedKey, value));
    }

    @Override
    public BatchValue offer(List<ContentKey> keys, List<?> values) {
        for (int i = 0; i < keys.size(); i++) {
            this.offer(keys.get(i), values.get(i));
        }

        return new GuardianBatchValue(keys, this).setElements(values).setDirty(true);
    }

    @Override
    public <E> Optional<SingleValue<E>> get(ContentKey key) {
        if (!this.keySet.contains(key)) return Optional.empty();
        if (!this.container.containsKey(key.getId())) return Optional.empty();

        return Optional.ofNullable((SingleValue<E>) this.container.get(key.getId()));
    }

    @Override
    public <E> Optional<SingleValue<E>> get(String id) {
        Optional<ContentKey> contentKey = this.getPossibleKeys().stream()
                .filter(key -> key.getId().equals(id))
                .findFirst();

        return contentKey.map(identifiedKey -> this.<E>get(identifiedKey).orElse(null));
    }

    @Override
    public Set<ContentKey> getKeys() {
        return this.keySet;
    }

    @Override
    public Set<?> getValues() {
        return Sets.newHashSet(this.container.values());
    }

    @Override
    public Optional<ContentLoader> getContentLoader() {
        return Optional.ofNullable(this.contentLoader);
    }
}
