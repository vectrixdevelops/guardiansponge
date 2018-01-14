/*
 * MIT License
 *
 * Copyright (c) 2017 Connor Hartley
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.ichorpowered.guardian.common.capture;

import com.google.common.collect.Lists;
import com.ichorpowered.guardian.sequence.GuardianSequence;
import com.ichorpowered.guardian.sequence.capture.AbstractCapture;
import com.ichorpowered.guardianapi.content.ContentKeys;
import com.ichorpowered.guardianapi.content.transaction.ContentKey;
import com.ichorpowered.guardianapi.detection.Detection;
import com.ichorpowered.guardianapi.detection.capture.CaptureContainer;
import com.ichorpowered.guardianapi.entry.entity.PlayerEntry;
import com.ichorpowered.guardianapi.util.key.NamedTypeKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.property.ArmorSlotType;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

public class PlayerBlacklistCapture extends AbstractCapture {

    private static final String CLASS_NAME = PlayerBlacklistCapture.class.getSimpleName().toUpperCase();

    public static NamedTypeKey<Boolean> PASS_FILTER =
            NamedTypeKey.of(CLASS_NAME + ":passFilter", Boolean.class);

    private List<ItemType> blacklistItems = Lists.newArrayList();
    private List<ItemType> blacklistEquipment = Lists.newArrayList();

    public PlayerBlacklistCapture(@Nonnull Object plugin, @Nonnull Detection detection) {
        super(plugin, detection);

        if (detection.getContentContainer().get(ContentKeys.EQUIPMENT_BLACKLIST).isPresent()) {
            List<String> ids = detection.getContentContainer().get(ContentKeys.EQUIPMENT_BLACKLIST).get()
                    .getElement().orElse(Lists.newArrayList());

            for (String id : ids) {
                Sponge.getRegistry().getType(ItemType.class, id).ifPresent(itemType -> {
                    this.blacklistEquipment.add(itemType);
                });
            }
        }

        if (detection.getContentContainer().get(ContentKeys.ITEM_BLACKLIST).isPresent()) {
            List<String> ids = detection.getContentContainer().get(ContentKeys.ITEM_BLACKLIST).get()
                    .getElement().orElse(Lists.newArrayList());

            for (String id : ids) {
                Sponge.getRegistry().getType(ItemType.class, id).ifPresent(itemType -> {
                    this.blacklistItems.add(itemType);
                });
            }
        }
    }

    @Override
    public void update(@Nonnull PlayerEntry entry, @Nonnull CaptureContainer captureContainer) {
        if (!entry.getEntity(Player.class).isPresent() || !captureContainer.get(GuardianSequence.INITIAL_LOCATION).isPresent()) return;
        final Player player = entry.getEntity(Player.class).get();

        captureContainer.transform(PlayerBlacklistCapture.PASS_FILTER, original -> original, true);

        this.blacklistItems.forEach(itemType -> {
            if (player.getInventory().contains(itemType)) {
                captureContainer.put(PlayerBlacklistCapture.PASS_FILTER, false);
            }
        });

        this.blacklistEquipment.forEach(itemType -> {
            Optional<ArmorSlotType> armorSlotType = InventoryArchetypes.PLAYER.getProperty(ArmorSlotType.class);

            if (armorSlotType.isPresent() && player.getInventory().query(QueryOperationTypes.INVENTORY_PROPERTY.of(armorSlotType.get())).contains(itemType)) {
                captureContainer.put(PlayerBlacklistCapture.PASS_FILTER, false);
            }
        });
    }
}
