package dev.osmii.shadow

import dev.osmii.shadow.enums.GamePhase
import dev.osmii.shadow.enums.PlayableRole
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.HashMap

class ShadowGameState {
    var currentPhase: GamePhase = GamePhase.NONE

    var participationStatus = HashMap<UUID, Boolean>()

    var originalRoles: HashMap<UUID, PlayableRole> = HashMap<UUID, PlayableRole>()
    var currentRoles: HashMap<UUID, PlayableRole> = HashMap<UUID, PlayableRole>()
    var currentWinners: HashSet<Player> = HashSet<Player>()
}