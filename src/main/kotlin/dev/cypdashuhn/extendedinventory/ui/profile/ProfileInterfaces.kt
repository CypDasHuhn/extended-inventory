package dev.cypdashuhn.extendedinventory.ui.profile

import dev.cypdashuhn.extendedinventory.ExtendedInventoryPlugin
import dev.cypdashuhn.extendedinventory.actions.ProfileActions
import dev.cypdashuhn.extendedinventory.db.PlayerProfileManager
import dev.cypdashuhn.extendedinventory.db.PlayerProfileStatus
import dev.cypdashuhn.extendedinventory.db.ProfileManager
import dev.cypdashuhn.extendedinventory.db.ProfileOpenness
import dev.cypdashuhn.extendedinventory.hotbar.HotbarManager
import dev.cypdashuhn.extendedinventory.ui.ChatInputManager
import dev.cypdashuhn.extendedinventory.ui.inventory.InventoryInterface
import dev.cypdashuhn.extendedinventory.ui.inventory.InventoryInterfaceContext
import dev.cypdashuhn.extendedinventory.util.mm
import dev.rooster.core.util.createItem
import dev.rooster.ui.interfaces.ClickInfo
import dev.rooster.ui.interfaces.InterfaceInfo
import dev.rooster.ui.interfaces.constructors.indexed_content.ScrollContext
import dev.rooster.ui.interfaces.constructors.indexed_content.ScrollInterface
import dev.rooster.ui.interfaces.constructors.indexed_content.ScrollInterfaceOptions
import dev.rooster.ui.interfaces.constructors.indexed_content.sizeFromRows
import dev.rooster.ui.interfaces.handler
import dev.rooster.ui.items.InterfaceItem
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.transactions.transaction

class ProfileInterfaceContext(
    var cachedProfiles: List<ProfileEntryData> = emptyList(),
    var currentPrimaryId: Int? = null,
) : ScrollContext() {
    fun copy(): ProfileInterfaceContext {
        val c = ProfileInterfaceContext(cachedProfiles, currentPrimaryId)
        c.position = this.position
        return c
    }
}

data class ProfileEntryData(
    val id: Int,
    val name: String,
    val createdByPlayerId: Int,
    val openness: ProfileOpenness,
)

private fun refreshProfiles(player: Player, context: ProfileInterfaceContext) {
    context.cachedProfiles = transaction {
        val profiles = ProfileManager.allAccessible(player)
        profiles.map { ProfileEntryData(it.id.value, it.name, it.createdByPlayerId, it.openness) }
    }
    context.currentPrimaryId = try {
        PlayerProfileManager.getPrimary(player)
    } catch (_: Exception) { null }
}

object ProfileInterface : ScrollInterface<ProfileInterfaceContext, ProfileEntryData>(
    handler { ProfileInterfaceContext() },
    ScrollInterfaceOptions<ProfileInterfaceContext>().apply {
        inventoryTitle = { _, _ -> mm("<white><bold>Profiles") }
        sizeFromRows(6)
    },
) {
    override fun contentProvider(id: Int, context: ProfileInterfaceContext): ProfileEntryData? {
        return context.cachedProfiles.getOrNull(id)
    }

    override fun contentDisplay(data: ProfileEntryData, context: ProfileInterfaceContext): InterfaceInfo<ProfileInterfaceContext>.() -> ItemStack = {
        val isPrimary = context.currentPrimaryId == data.id
        createItem(
            if (isPrimary) Material.ENCHANTED_BOOK else Material.BOOK,
            mm(if (isPrimary) "<green>${data.name} (Primary)" else "<white>${data.name}"),
            listOf(
                mm("<gray>Openness: ${data.openness.name.lowercase()}"),
                mm("<yellow>Click to view details"),
            ),
        )
    }

    override fun contentClick(data: ProfileEntryData, context: ProfileInterfaceContext): ClickInfo<ProfileInterfaceContext>.() -> Unit = {
        ProfileDetailInterface.openInventory(click.player, ProfileDetailContext(data.id, data.name, data.createdByPlayerId, data.openness))
    }

    override fun getInterfaceItems(): List<InterfaceItem<ProfileInterfaceContext>> = listOf(
        item()
            .atSlot(6, 1)
            .displayAs(createItem(Material.BARRIER, mm("<red>Back"), listOf(mm("<gray>Return to inventory."))))
            .onClick { click.player.closeInventory() },

        item()
            .atSlot(6, 4)
            .displayAs(createItem(Material.WRITABLE_BOOK, mm("<white>New Profile"), listOf(mm("<gray>Create a new profile."))))
            .onClick {
                ChatInputManager.awaitInput(click.player, "<gray>Type the profile <white>name<gray>:") { name ->
                    if (name.isBlank()) {
                        ProfileInterface.openRefreshed(click.player, context)
                        return@awaitInput
                    }
                    ProfileActions.createProfile(click.player, name.trim())
                    ProfileInterface.openRefreshed(click.player, context)
                }
            },
    )

    fun openRefreshed(player: Player, context: ProfileInterfaceContext): org.bukkit.inventory.Inventory {
        refreshProfiles(player, context)
        return openInventory(player, context.copy())
    }
}

class ProfileDetailContext(
    val profileId: Int,
    val profileName: String,
    val createdByPlayerId: Int,
    val openness: ProfileOpenness,
) : ScrollContext()

object ProfileDetailInterface : ScrollInterface<ProfileDetailContext, ProfileEntryData>(
    handler { ProfileDetailContext(0, "", 0, ProfileOpenness.PRIVATE) },
    ScrollInterfaceOptions<ProfileDetailContext>().apply {
        inventoryTitle = { _, ctx -> mm("<white><bold>Profile: ${ctx.profileName}") }
        sizeFromRows(6)
    },
) {
    override fun contentProvider(id: Int, context: ProfileDetailContext): ProfileEntryData? = null
    override fun contentDisplay(data: ProfileEntryData, context: ProfileDetailContext): InterfaceInfo<ProfileDetailContext>.() -> ItemStack = { ItemStack(Material.AIR) }
    override fun contentClick(data: ProfileEntryData, context: ProfileDetailContext): ClickInfo<ProfileDetailContext>.() -> Unit = {}

    override fun getInterfaceItems(): List<InterfaceItem<ProfileDetailContext>> = listOf(
        item()
            .atSlot(6, 1)
            .displayAs(createItem(Material.BARRIER, mm("<red>Back"), listOf(mm("<gray>Return to profile list."))))
            .onClick { ProfileInterface.openRefreshed(click.player, ProfileInterfaceContext()) },

        item()
            .atSlot(2, 4)
            .displayAs {
                val ctx = context
                createItem(Material.BOOK, mm("<green>Info"), listOf(
                    mm("<white>Name: ${ctx.profileName}"),
                    mm("<white>Openness: ${ctx.openness.name.lowercase()}"),
                ))
            },

        item()
            .atSlot(2, 5)
            .displayAs(createItem(Material.ENDER_PEARL, mm("<green>Switch To"), listOf(mm("<gray>Make this your active profile."))))
            .onClick {
                ProfileActions.switchProfile(click.player, context.profileId)
                HotbarManager.getState(click.player).profileId = context.profileId
                HotbarManager.mirrorToHotbar(click.player)
                InventoryInterface.openInventory(click.player, InventoryInterfaceContext(context.profileId))
            },

        item()
            .atSlot(2, 6)
            .displayAs(createItem(Material.BOOKSHELF, mm("<yellow>Set Default"), listOf(mm("<gray>Make this your default profile."))))
            .onClick {
                ProfileActions.setDefault(click.player, context.profileId)
                ProfileDetailInterface.openInventory(click.player, context)
            },

        item()
            .atSlot(2, 7)
            .displayAs(createItem(Material.NAME_TAG, mm("<yellow>Rename"), listOf(mm("<gray>Rename this profile."))))
            .onClick {
                ChatInputManager.awaitInput(click.player, "<gray>Type the new <white>name<gray>:") { newName ->
                    if (newName.isBlank()) return@awaitInput
                    ProfileManager.rename(context.profileId, newName.trim())
                    ProfileInterface.openRefreshed(click.player, ProfileInterfaceContext())
                }
            },

        item()
            .atSlot(3, 4)
            .displayAs {
                val ctx = context
                createItem(Material.REPEATER, mm("<yellow>Openness"), listOf(
                    mm("<gray>Current: ${ctx.openness.name.lowercase()}"),
                    mm("<yellow>Click to cycle: PRIVATE → PUBLIC_READ → PUBLIC_WRITE"),
                ))
            }.onClick {
                val next = when (context.openness) {
                    ProfileOpenness.PRIVATE -> ProfileOpenness.PUBLIC_READ
                    ProfileOpenness.PUBLIC_READ -> ProfileOpenness.PUBLIC_WRITE
                    ProfileOpenness.PUBLIC_WRITE -> ProfileOpenness.PRIVATE
                }
                ProfileActions.setOpenness(context.profileId, next)
                ProfileDetailInterface.openInventory(click.player, ProfileDetailContext(
                    context.profileId, context.profileName, context.createdByPlayerId, next
                ))
            },

        item()
            .atSlot(3, 5)
            .displayAs(createItem(Material.PLAYER_HEAD, mm("<white>Invitations"), listOf(mm("<gray>Manage player access."))))
            .routeTo(PlayerInviteInterface) { PlayerInviteContext(context.profileId) },

        item()
            .atSlot(3, 6)
            .displayAs(createItem(Material.LAVA_BUCKET, mm("<red>Delete"), listOf(mm("<gray>Delete this profile."))))
            .onClick {
                ProfileManager.delete(context.profileId)
                ProfileInterface.openRefreshed(click.player, ProfileInterfaceContext())
            },
    )
}

class PlayerInviteContext(
    val profileId: Int,
) : ScrollContext() {
    fun copy(): PlayerInviteContext {
        val c = PlayerInviteContext(profileId)
        c.position = this.position
        return c
    }
}

data class PlayerInviteData(
    val playerId: Int,
    val playerName: String,
    val status: PlayerProfileStatus,
)

object PlayerInviteInterface : ScrollInterface<PlayerInviteContext, PlayerInviteData>(
    handler { PlayerInviteContext(0) },
    ScrollInterfaceOptions<PlayerInviteContext>().apply {
        inventoryTitle = { _, _ -> mm("<white><bold>Player Access") }
        sizeFromRows(6)
    },
) {
    override fun contentProvider(id: Int, context: PlayerInviteContext): PlayerInviteData? {
        val rows = PlayerProfileManager.profilePlayers(context.profileId)
        return rows.getOrNull(id)?.let {
            val name = ExtendedInventoryPlugin.playerManager.players()
                .find { p -> p.id.value == it.playerId }?.name ?: "Unknown"
            PlayerInviteData(it.playerId, name, it.status)
        }
    }

    override fun contentDisplay(data: PlayerInviteData, context: PlayerInviteContext): InterfaceInfo<PlayerInviteContext>.() -> ItemStack = {
        val statusColor = when (data.status) {
            PlayerProfileStatus.PRIMARY -> "<green>"
            PlayerProfileStatus.WRITE_READ -> "<yellow>"
            PlayerProfileStatus.READ_ONLY -> "<gray>"
        }
        createItem(
            Material.PLAYER_HEAD,
            mm("<white>${data.playerName}"),
            listOf(
                mm("${statusColor}Status: ${data.status.name}"),
                mm("<yellow>Click to cycle access."),
            ),
        )
    }

    override fun contentClick(data: PlayerInviteData, context: PlayerInviteContext): ClickInfo<PlayerInviteContext>.() -> Unit = {
        val next = when (data.status) {
            PlayerProfileStatus.PRIMARY -> PlayerProfileStatus.READ_ONLY
            PlayerProfileStatus.WRITE_READ -> PlayerProfileStatus.READ_ONLY
            PlayerProfileStatus.READ_ONLY -> PlayerProfileStatus.WRITE_READ
        }
        val onlinePlayer = Bukkit.getPlayer(data.playerName)
        if (onlinePlayer != null) {
            PlayerProfileManager.assign(onlinePlayer, context.profileId, next)
        }
        PlayerInviteInterface.openInventory(click.player, context.copy())
    }

    override fun getInterfaceItems(): List<InterfaceItem<PlayerInviteContext>> = listOf(
        item()
            .atSlot(6, 1)
            .displayAs(createItem(Material.BARRIER, mm("<red>Back"), listOf(mm("<gray>Return to profile detail."))))
            .onClick { click.player.closeInventory() },
    )
}
