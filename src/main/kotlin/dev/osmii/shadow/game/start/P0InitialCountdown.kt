package dev.osmii.shadow.game.start

import dev.osmii.shadow.Shadow
import dev.osmii.shadow.enums.GamePhase
import dev.osmii.shadow.enums.Namespace
import dev.osmii.shadow.util.ItemUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.title.Title
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.*
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitTask
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

class P0InitialCountdown(private var shadow: Shadow) {
    fun startCountdown() {
        shadow.gameState.currentRoles.clear()
        shadow.gameState.currentWinners.clear()
        for (player in shadow.server.onlinePlayers) {
            shadow.gameState.participationStatus.putIfAbsent(player.uniqueId, true)

            player.sendMessage(
                MiniMessage.miniMessage().deserialize("<green>The game will start in 10 seconds!</green>" +
                    "\n<i><gray>You can toggle your participation status by clicking the ender eye in your hotbar.</gray></i>")
            )

            val participationToggle = ItemStack(Material.ENDER_EYE)
            participationToggle.itemMeta = participationToggle.itemMeta.apply {
                if (this == null) return@apply

                this.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                if (shadow.gameState.participationStatus[player.uniqueId]!!) {
                    this.displayName(
                        MiniMessage.miniMessage().deserialize("<gray>Participation: <green>Participating</green></gray>")
                    )
                    this.addEnchant(Enchantment.DAMAGE_ALL, 1, true)
                } else {
                    this.displayName(
                        MiniMessage.miniMessage().deserialize("<gray>Participation: <red>Not Participating</red></gray>")
                    )
                }

                this.persistentDataContainer.set(
                    Namespace.FORBIDDEN,
                    PersistentDataType.BYTE_ARRAY,
                    ItemUtil.forbidden(drop = true, use = true, move = true)
                )
                this.persistentDataContainer.set(Namespace.CUSTOM_ID, PersistentDataType.STRING, "participation-toggle")
            }

            player.inventory.setItem(8, participationToggle)
        }

        // Send countdown title
        val secsLeft = AtomicInteger(10)
        val task: BukkitTask = Bukkit.getScheduler().runTaskTimer(shadow, Runnable {
            val color = when (secsLeft.get()) {
                6, 5, 4 -> NamedTextColor.YELLOW
                3, 2, 1 -> NamedTextColor.RED
                else -> NamedTextColor.GREEN
            }

            for (player in shadow.server.onlinePlayers) {
                player.showTitle(
                    Title.title(
                        Component.text("${secsLeft.get()}")
                            .color(color),
                        Component.empty(),
                        Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(1100), Duration.ofMillis(0))
                    )
                )
            }

            secsLeft.getAndDecrement()
        }, 0, 20L)

        // Start game
        Bukkit.getScheduler().runTaskLater(shadow, Runnable {
            task.cancel()

            shadow.server.onlinePlayers.forEach() { player ->
                player.inventory.clear()
            }

            shadow.gameState.currentPhase = GamePhase.ROLES_ASSIGNED
            P1AssignRoles(shadow).assignRoles()
        }, 200)
    }
}