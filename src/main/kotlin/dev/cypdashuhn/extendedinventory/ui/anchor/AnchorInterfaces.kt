package dev.cypdashuhn.extendedinventory.ui.anchor

import dev.cypdashuhn.extendedinventory.actions.AnchorActions
import dev.cypdashuhn.extendedinventory.db.AnchorManager
import dev.cypdashuhn.extendedinventory.hotbar.HotbarManager
import dev.cypdashuhn.extendedinventory.ui.ChatInputManager
import dev.cypdashuhn.extendedinventory.ui.inventory.InventoryInterface
import dev.cypdashuhn.extendedinventory.ui.inventory.InventoryInterfaceContext
import dev.cypdashuhn.extendedinventory.ui.inventory.InterfaceMode
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
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.transactions.transaction

class AnchorListContext(
    var profileId: Int,
) : ScrollContext()

data class AnchorEntryData(
    val id: Int,
    val name: String,
    val x: Int,
    val y: Int,
)

object AnchorListInterface : ScrollInterface<AnchorListContext, AnchorEntryData>(
    "AnchorList",
    handler { AnchorListContext(0) },
    ScrollInterfaceOptions<AnchorListContext>().apply {
        inventoryTitle = { _, _ -> mm("<white><bold>Anchors") }
        sizeFromRows(6)
    },
) {
    override fun contentProvider(id: Int, context: AnchorListContext): AnchorEntryData? = transaction {
        val anchors = AnchorManager.allForProfile(context.profileId)
        anchors.getOrNull(id)?.let { AnchorEntryData(it.id, it.name, it.x, it.y) }
    }

    override fun contentDisplay(data: AnchorEntryData, context: AnchorListContext): InterfaceInfo<AnchorListContext>.() -> ItemStack = {
        createItem(
            Material.ENDER_PEARL,
            mm("<white>${data.name}"),
            listOf(
                mm("<gray>Position: (${data.x}, ${data.y})"),
                mm("<yellow>Click to view details"),
            ),
        )
    }

    override fun contentClick(data: AnchorEntryData, context: AnchorListContext): ClickInfo<AnchorListContext>.() -> Unit = {
        AnchorDetailInterface.openInventory(click.player, AnchorDetailContext(context.profileId, data.id, data.name, data.x, data.y))
    }

    override fun getInterfaceItems(): List<InterfaceItem<AnchorListContext>> = listOf(
        item()
            .atSlot(6, 1)
            .displayAs(createItem(Material.BARRIER, mm("<red>Back"), listOf(mm("<gray>Return to inventory."))))
            .routeTo(InventoryInterface) { InventoryInterfaceContext(context.profileId) },

        item()
            .atSlot(6, 4)
            .displayAs(createItem(Material.WRITABLE_BOOK, mm("<white>New Anchor"), listOf(mm("<gray>Create a new anchor at current position."))))
            .onClick {
                val player = click.player
                val state = HotbarManager.getState(player)
                ChatInputManager.awaitInput(player, "<gray>Type the anchor <white>name<gray>:") { name ->
                    if (name.isBlank()) {
                        AnchorListInterface.openInventory(player, context)
                        return@awaitInput
                    }
                    AnchorActions.addAnchor(context.profileId, name.trim(), state.x, state.y)
                    AnchorListInterface.openInventory(player, context)
                }
            },
    )
}

class AnchorDetailContext(
    val profileId: Int,
    val anchorId: Int,
    val anchorName: String,
    val anchorX: Int,
    val anchorY: Int,
) : ScrollContext()

object AnchorDetailInterface : ScrollInterface<AnchorDetailContext, AnchorEntryData>(
    "AnchorDetail",
    handler { AnchorDetailContext(0, 0, "", 0, 0) },
    ScrollInterfaceOptions<AnchorDetailContext>().apply {
        inventoryTitle = { _, ctx -> mm("<white><bold>Anchor: ${ctx.anchorName}") }
        sizeFromRows(6)
    },
) {
    override fun contentProvider(id: Int, context: AnchorDetailContext): AnchorEntryData? = null

    override fun contentDisplay(data: AnchorEntryData, context: AnchorDetailContext): InterfaceInfo<AnchorDetailContext>.() -> ItemStack = {
        ItemStack(Material.AIR)
    }

    override fun contentClick(data: AnchorEntryData, context: AnchorDetailContext): ClickInfo<AnchorDetailContext>.() -> Unit = {}

    override fun getInterfaceItems() = listOf(
        item()
            .atSlot(6, 1)
            .displayAs(createItem(Material.BARRIER, mm("<red>Back"), listOf(mm("<gray>Return to anchor list."))))
            .routeTo(AnchorListInterface) { AnchorListContext(context.profileId) },

        item()
            .atSlot(3, 4)
            .displayAs {
                val ctx = context
                createItem(Material.ENDER_PEARL, mm("<green>Info"), listOf(
                    mm("<white>Name: ${ctx.anchorName}"),
                    mm("<white>Position: (${ctx.anchorX}, ${ctx.anchorY})"),
                ))
            },

        item()
            .atSlot(3, 5)
            .displayAs {
                val ctx = context
                createItem(Material.COMPASS, mm("<green>Jump To"), listOf(
                    mm("<gray>Jump to (${ctx.anchorX}, ${ctx.anchorY})"),
                ))
            }.onClick {
                HotbarManager.jumpTo(click.player, context.anchorX, context.anchorY)
                InventoryInterface.openInventory(click.player, InventoryInterfaceContext(context.profileId, context.anchorX, context.anchorY))
            },

        item()
            .atSlot(3, 6)
            .displayAs(createItem(Material.NAME_TAG, mm("<yellow>Rename"), listOf(mm("<gray>Rename this anchor."))))
            .onClick {
                ChatInputManager.awaitInput(click.player, "<gray>Type the new <white>name<gray>:") { newName ->
                    if (newName.isBlank()) {
                        AnchorDetailInterface.openInventory(click.player, context)
                        return@awaitInput
                    }
                    AnchorActions.renameAnchor(click.player, context.profileId, context.anchorName, newName.trim())
                    AnchorListInterface.openInventory(click.player, AnchorListContext(context.profileId))
                }
            },

        item()
            .atSlot(3, 7)
            .displayAs(createItem(Material.LAVA_BUCKET, mm("<red>Delete"), listOf(mm("<gray>Delete this anchor."))))
            .onClick {
                AnchorActions.deleteAnchor(click.player, context.profileId, context.anchorName)
                AnchorListInterface.openInventory(click.player, AnchorListContext(context.profileId))
            },

        item()
            .atSlot(4, 5)
            .displayAs(createItem(Material.ITEM_FRAME, mm("<white>Materialize"), listOf(mm("<gray>Get a materialized anchor item."))))
            .onClick {
                val anchorItem = HotbarManager.createAnchorItem(context.anchorName, context.anchorX, context.anchorY)
                click.player.inventory.addItem(anchorItem)
            },
    )
}
