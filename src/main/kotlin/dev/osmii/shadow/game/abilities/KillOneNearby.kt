package dev.osmii.shadow.game.abilities

import dev.osmii.shadow.Shadow
import dev.osmii.shadow.enums.PlayableRole
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class KillOneNearby : Ability {
    override val item: ItemStack = ItemStack(Material.NETHERITE_SWORD)
    init {
        val meta = item.itemMeta
        meta.lore(listOf(Component.text("Kills a random player within")))
        meta.displayName(Component.text("Kill A Nearby Player").color(TextColor.color(135,0,0)))
        item.itemMeta = meta
    }
    override fun apply(player: Player, shadow: Shadow) {
        // If a server continually runs for around 3.4 years this breaks
        if(cooldownMap[player] != null && shadow.server.currentTick > cooldownMap[player]!!) {
            player.sendMessage(
                MiniMessage.miniMessage().deserialize(
                "<red>Your kill ability is on cooldown for ${
                    (COOLDOWN - (shadow.server.currentTick - cooldownMap[player]!!))/20} seconds</red>")
            )
            return
        }
        val targets = player.world.getNearbyPlayers(player.location,18.0)

        targets.remove(player)

        targets.removeIf { shadow.gameState.currentRoles[player.uniqueId] == PlayableRole.SPECTATOR}

        if(targets.isNotEmpty()) {
            val killed = targets.sortedBy { target : Player ->
                player.location.distance(target.location)
            }
            killed[0].health = 0.0
            killed[0].sendHealthUpdate()
            player.sendMessage(Component.text("Killed ").append(killed[0].displayName()))
            cooldownMap[player] = shadow.server.currentTick + COOLDOWN
        } else {
            player.sendMessage(
                MiniMessage.miniMessage().deserialize("<red>No nearby players to kill</red>")
            )
        }

    }
    companion object {
        private const val COOLDOWN = 7*60*20
        val cooldownMap = HashMap<Player,Int>()
    }
}