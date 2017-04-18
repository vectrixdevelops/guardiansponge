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
package io.github.connorhartley.guardian.command;

import io.github.connorhartley.guardian.Guardian;
import io.github.connorhartley.guardian.GuardianInfo;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

public class InfoCommand extends AbstractCommand {

    public InfoCommand(Guardian plugin) {
        super(plugin);
    }

    public static CommandSpec createCommand(Guardian plugin) {
        return CommandSpec.builder()
                .description(Text.of("Shows basic running information about Guardian."))
                .permission("guardian.general.info")
                .executor(new InfoCommand(plugin))
                .build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof User) {
            src.sendMessage(Text.of(TextColors.AQUA, GuardianInfo.NAME, " v", GuardianInfo.VERSION));
            src.sendMessage(Text.of(TextColors.GRAY, "Built with love by ", StringUtils.join(this.getPlugin().getPluginContainer().getAuthors(), ", ")));
            src.sendMessage(Text.of(TextColors.GRAY, "Running ", this.getPlugin().getModuleController().getModules().size(), " modules."));
            src.sendMessage(Text.builder("Click here for help.")
                    .color(TextColors.AQUA)
                    .onClick(TextActions.runCommand("/guardian help"))
                    .onHover(TextActions.showText(Text.of("/guardian help")))
                    .build());
        } else if (src instanceof ConsoleSource) {

        }

        return CommandResult.success();
    }
}
