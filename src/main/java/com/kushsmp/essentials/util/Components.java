package com.kushsmp.essentials.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Bridges the plugin's legacy section-sign colored strings (produced by
 * TextUtil.color, including the bungee "§x" hex format) into Adventure
 * Components, which Paper's tab-list and title APIs require.
 */
public final class Components {

    // Section char (§) serializer that understands both legacy & codes and the
    // "§x§r§r§g§g§b§b" hex format that TextUtil emits for &#RRGGBB colors.
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('\u00A7')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private Components() {
    }

    /** Convert an already-colored section-sign string into a Component. */
    public static Component of(String legacy) {
        return LEGACY.deserialize(legacy == null ? "" : legacy);
    }
}
