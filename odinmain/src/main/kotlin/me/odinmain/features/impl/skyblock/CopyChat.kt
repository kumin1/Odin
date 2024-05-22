package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.copyToClipboard
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.scaleFactor
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.client.gui.GuiChat
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import kotlin.math.floor

object CopyChat : Module(
    name = "Copy Chat",
    category = Category.SKYBLOCK,
    description = "Allows you to right click messages in chat to copy them.",
) {
    private val keybind: Keybinding by KeybindSetting("Keybind", Keyboard.KEY_LCONTROL, "Hold to copy message with color codes")
    private val sendMessage: Boolean by BooleanSetting("Send Message", false, description =  "Sends the message you copied in chat.")

    @SubscribeEvent
    fun mouseClicked(event: GuiEvent.GuiMouseClickEvent) {
        if (event.button != 1 || mc.currentScreen !is GuiChat) return

        val chatGui = mc.ingameGUI?.chatGUI ?: return
        val maxChatWidth = floor(scaleFactor * 280 + 320).toInt()
        val components = mutableSetOf<String>()

        for (x in 0 until maxChatWidth step 10) {
            val scannedComponent = chatGui.getChatComponent(x, Mouse.getY())?.unformattedTextForChat ?: continue
            components.add(scannedComponent)
        }
        val message = components.joinToString(separator = "") { it }

        copyToClipboard(if (keybind.isDown()) message else message.noControlCodes)
        modMessage(if (sendMessage) "§7${message.noControlCodes}" else "§aCopied chat message to clipboard!")
    }
}