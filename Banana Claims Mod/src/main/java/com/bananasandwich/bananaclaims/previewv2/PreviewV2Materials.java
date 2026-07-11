package com.bananasandwich.bananaclaims.previewv2;

import com.bananasandwich.bananaclaims.Bananaclaims;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Resolves configured block identifiers once per config load.
 *
 * <p>The renderer receives already-resolved block states, avoiding registry
 * lookups for every spawned display entity.</p>
 */
public final class PreviewV2Materials {

    private final ResolvedMaterial border;
    private final ResolvedMaterial corner;
    private final ResolvedMaterial guide;

    PreviewV2Materials(
            PreviewV2Config config
    ) {
        border = resolve(
                config.getBorder().getMaterial(),
                PreviewV2Config.DEFAULT_BORDER_MATERIAL,
                Blocks.AMETHYST_BLOCK,
                "border"
        );

        corner = resolve(
                config.getCorners().getMaterial(),
                PreviewV2Config.DEFAULT_CORNER_MATERIAL,
                Blocks.LAPIS_BLOCK,
                "corner"
        );

        guide = resolve(
                config.getGuides().getMaterial(),
                PreviewV2Config.DEFAULT_GUIDE_MATERIAL,
                Blocks.AMETHYST_BLOCK,
                "guide"
        );
    }

    public BlockState getBorderState() {
        return border.state();
    }

    public BlockState getCornerState() {
        return corner.state();
    }

    public BlockState getGuideState() {
        return guide.state();
    }

    String getBorderIdentifier() {
        return border.identifier();
    }

    String getCornerIdentifier() {
        return corner.identifier();
    }

    String getGuideIdentifier() {
        return guide.identifier();
    }

    private static ResolvedMaterial resolve(
            String configuredIdentifier,
            String fallbackIdentifier,
            Block fallbackBlock,
            String settingName
    ) {
        Identifier identifier =
                parseIdentifier(configuredIdentifier);

        if (identifier != null
                && BuiltInRegistries.BLOCK.containsKey(identifier)) {
            Block block =
                    BuiltInRegistries.BLOCK.getValue(identifier);

            if (block != null
                    && block != Blocks.AIR) {
                return new ResolvedMaterial(
                        identifier.toString(),
                        block.defaultBlockState()
                );
            }
        }

        Bananaclaims.LOGGER.warn(
                "Invalid Renderer V2 {} material '{}'. Using '{}'.",
                settingName,
                configuredIdentifier,
                fallbackIdentifier
        );

        return new ResolvedMaterial(
                fallbackIdentifier,
                fallbackBlock.defaultBlockState()
        );
    }

    private static Identifier parseIdentifier(
            String value
    ) {
        if (value == null
                || value.isBlank()) {
            return null;
        }

        String trimmed =
                value.trim();

        int separator =
                trimmed.indexOf(':');

        String namespace;
        String path;

        if (separator < 0) {
            namespace = "minecraft";
            path = trimmed;
        } else {
            namespace = trimmed.substring(0, separator);
            path = trimmed.substring(separator + 1);
        }

        if (namespace.isBlank()
                || path.isBlank()
                || path.indexOf(':') >= 0) {
            return null;
        }

        try {
            return Identifier.fromNamespaceAndPath(
                    namespace,
                    path
            );
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private record ResolvedMaterial(
            String identifier,
            BlockState state
    ) {
    }
}
