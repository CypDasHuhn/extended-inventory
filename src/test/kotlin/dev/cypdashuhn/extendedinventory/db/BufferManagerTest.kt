package dev.cypdashuhn.extendedinventory.db

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.UUID

class BufferManagerTest {

    private fun mockPlayer(uuid: UUID = UUID.randomUUID()): org.bukkit.entity.Player {
        return MockPlayer(uuid)
    }

    @Test
    fun `push and pop buffer entry`() {
        val player = mockPlayer()
        val items = listOf(ItemStack(Material.STONE), ItemStack(Material.DIRT), null, ItemStack(Material.OAK_PLANKS))
        val position = 5 to 10

        BufferManager.push(player, items, position)
        val entry = BufferManager.pop(player)

        assertNotNull(entry)
        assertEquals(position, entry!!.position)
        assertEquals(4, entry.items.size)
        assertEquals(Material.STONE, entry.items[0]?.type)
        assertEquals(Material.DIRT, entry.items[1]?.type)
        assertNull(entry.items[2])
        assertEquals(Material.OAK_PLANKS, entry.items[3]?.type)
    }

    @Test
    fun `pop empty buffer returns null`() {
        val entry = BufferManager.pop(mockPlayer())
        assertNull(entry)
    }

    @Test
    fun `peek does not remove entry`() {
        val player = mockPlayer()
        BufferManager.push(player, listOf(ItemStack(Material.STONE)), 0 to 0)

        assertNotNull(BufferManager.peek(player))
        assertNotNull(BufferManager.pop(player))
        assertNull(BufferManager.pop(player))
    }

    @Test
    fun `buffer entries are stacked LIFO`() {
        val player = mockPlayer()
        BufferManager.push(player, listOf(ItemStack(Material.STONE)), 1 to 1)
        BufferManager.push(player, listOf(ItemStack(Material.DIRT)), 2 to 2)

        assertEquals(2 to 2, BufferManager.pop(player)!!.position)
        assertEquals(1 to 1, BufferManager.pop(player)!!.position)
    }

    @Test
    fun `buffer TTL expires old entries`() {
        val player = mockPlayer()
        BufferManager.setTtl(1)
        BufferManager.push(player, listOf(ItemStack(Material.STONE)), 0 to 0)
        Thread.sleep(5)
        assertNull(BufferManager.pop(player))
        BufferManager.setTtl(5 * 60 * 1000) // restore default
    }

    @Test
    fun `list returns all entries in LIFO order`() {
        val player = mockPlayer()
        BufferManager.push(player, listOf(ItemStack(Material.STONE)), 1 to 1)
        BufferManager.push(player, listOf(ItemStack(Material.DIRT)), 2 to 2)

        val list = BufferManager.list(player)
        assertEquals(2, list.size)
        assertEquals(2 to 2, list[0].position)
        assertEquals(1 to 1, list[1].position)
    }

    @Test
    fun `loadByName finds buffer by formatted timestamp`() {
        val player = mockPlayer()
        BufferManager.push(player, listOf(ItemStack(Material.STONE)), 3 to 3)

        val entries = BufferManager.list(player)
        assertTrue(entries.isNotEmpty())
        val name = BufferManager.formatTimestamp(entries[0].timestamp)

        val loaded = BufferManager.loadByName(player, name)
        assertNotNull(loaded)
        assertEquals(3 to 3, loaded!!.position)
    }

    @Test
    fun `clear removes all buffers`() {
        val player = mockPlayer()
        BufferManager.push(player, listOf(ItemStack(Material.STONE)), 0 to 0)
        BufferManager.clear(player)
        assertTrue(BufferManager.list(player).isEmpty())
    }

    @Test
    fun `buffer isolates per player`() {
        val p1 = mockPlayer(UUID.randomUUID())
        val p2 = mockPlayer(UUID.randomUUID())

        BufferManager.push(p1, listOf(ItemStack(Material.STONE)), 0 to 0)

        assertNull(BufferManager.pop(p2))
        assertNotNull(BufferManager.pop(p1))
    }
}

private class MockPlayer(private val id: UUID) : org.bukkit.entity.Player {
    override fun getUniqueId() = id
    override fun getName() = "TestPlayer-${id.toString().take(8)}"
    override fun getPlayer() = this
    override fun getLocation() = throw NotImplementedError()
    override fun getInventory() = throw NotImplementedError()
    override fun sendMessage(message: String?) {}
    override fun sendMessage(vararg messages: String?) {}
    override fun sendMessage(message: net.kyori.adventure.text.Component?) {}
    override fun sendMessage(component: net.kyori.adventure.text.Component, type: net.kyori.adventure.audience.MessageType) {}
    override fun sendMessage(identity: net.kyori.adventure.identity.Identity?, message: net.kyori.adventure.text.Component, type: net.kyori.adventure.audience.MessageType) {}
    override fun sendActionBar(message: net.kyori.adventure.text.Component) {}
    override fun sendActionBar(component: net.kyori.adventure.text.Component, type: net.kyori.adventure.audience.MessageType) {}
    override fun sendActionBar(identity: net.kyori.adventure.identity.Identity?, message: net.kyori.adventure.text.Component, type: net.kyori.adventure.audience.MessageType) {}
    override fun getClientBrand() = "mock"
    override fun getEffectivePermissions(): MutableSet<org.bukkit.permissions.PermissionAttachmentInfo> = mutableSetOf()
    override fun getActiveItem(): ItemStack = ItemStack(Material.AIR)
    override fun getCollidableExemptions(): MutableList<org.bukkit.entity.Entity> = mutableListOf()
    override fun getListeningPluginChannels(): MutableSet<String> = mutableSetOf()
    override fun getScoreboardDisplayName(): net.kyori.adventure.text.Component = net.kyori.adventure.text.Component.text("")
    override fun getSpectatorTarget(): org.bukkit.entity.Entity? = null
    override fun isOp() = false
    override fun setOp(value: Boolean) {}
    override fun isOnline() = true
    override fun isConnected() = true
    override fun isDead() = false
    override fun isValid() = true
    override fun getFallDistance() = 0f
    override fun setFallDistance(distance: Float) {}
    override fun getFireTicks() = 0
    override fun setFireTicks(ticks: Int) {}
    override fun getMaxFireTicks() = 20
    override fun remove() {}
    override fun getServer() = throw NotImplementedError()
    override fun getWorld() = throw NotImplementedError()
    override fun getType() = throw NotImplementedError()
    override fun getEntityId() = 0
    override fun isEmpty() = false
    override fun playEffect(loc: org.bukkit.Location, effect: org.bukkit.EntityEffect, data: Any?) {}
    override fun getTicksLived() = 0
    override fun setTicksLived(value: Int) {}
    override fun getNoDamageTicks() = 0
    override fun setNoDamageTicks(ticks: Int) {}
    override fun getLastDamageCause() = null
    override fun setLastDamageCause(event: org.bukkit.event.entity.EntityDamageEvent?) {}
    override fun getWorldBorder() = throw NotImplementedError()
    override fun isInsideVehicle() = false
    override fun leaveVehicle() = false
    override fun getVehicle() = null
    override fun setCustomNameVisible(flag: Boolean) {}
    override fun isCustomNameVisible() = false
    override fun setGlowing(flag: Boolean) {}
    override fun isGlowing() = false
    override fun setInvulnerable(flag: Boolean) {}
    override fun isInvulnerable() = false
    override fun isSilent() = false
    override fun setSilent(flag: Boolean) {}
    override fun hasGravity() = false
    override fun setGravity(gravity: Boolean) {}
    override fun getPortalCooldown() = 0
    override fun setPortalCooldown(cooldown: Int) {}
    override fun getScoreboardTags(): MutableSet<String> = mutableSetOf()
    override fun addScoreboardTag(tag: String) = false
    override fun removeScoreboardTag(tag: String) = false
    override fun getPistonMoveAsset() = throw NotImplementedError()
    override fun getHandRaised() = throw NotImplementedError()
    override fun getHandRaisedTime() = 0
    override fun setHandRaisedTime(ticks: Int) {}
    override fun isHandRaised() = false
    override fun getEquipment() = throw NotImplementedError()
    override fun getCategory() = throw NotImplementedError()
    override fun isInvisible() = false
    override fun setInvisible(invisible: Boolean) {}
    override fun getFirstPlayed() = 0L
    override fun getLastPlayed() = 0L
    override fun hasPlayedBefore() = false
    override fun getBedSpawnLocation() = null
    override fun setBedSpawnLocation(location: org.bukkit.Location?) {}
    override fun setBedSpawnLocation(location: org.bukkit.Location?, force: Boolean) {}
    override fun getAllowFlight() = false
    override fun setAllowFlight(flight: Boolean) {}
    override fun hidePlayer(plugin: org.bukkit.plugin.Plugin, player: org.bukkit.entity.Player) {}
    override fun showPlayer(plugin: org.bukkit.plugin.Plugin, player: org.bukkit.entity.Player) {}
    override fun canSee(player: org.bukkit.entity.Player) = true
    override fun getCompassTarget() = throw NotImplementedError()
    override fun setCompassTarget(location: org.bukkit.Location) {}
    override fun getAddress() = null
    override fun getClientViewDistance() = 10
    override fun getLocale() = "en_us"
    override fun getPing() = 0
    override fun getPlayerListName() = null
    override fun setPlayerListName(name: String?) {}
    override fun getPlayerListFooter() = null
    override fun setPlayerListFooter(footer: String?) {}
    override fun getPlayerListHeader() = null
    override fun setPlayerListHeader(header: String?) {}
    override fun getPlayerTime() = 0L
    override fun getPlayerTimeOffset() = 0L
    override fun isPlayerTimeRelative() = false
    override fun resetPlayerTime() {}
    override fun setPlayerTime(time: Long, relative: Boolean) {}
    override fun getPlayerWeather() = null
    override fun resetPlayerWeather() {}
    override fun setPlayerWeather(type: org.bukkit.WeatherType?) {}
    override fun giveExp(amount: Int) {}
    override fun giveExpLevels(levels: Int) {}
    override fun getExp() = 0f
    override fun setExp(exp: Float) {}
    override fun getLevel() = 0
    override fun setLevel(level: Int) {}
    override fun getTotalExperience() = 0
    override fun setTotalExperience(exp: Int) {}
    override fun sendExperienceChange(progress: Float) {}
    override fun sendExperienceChange(progress: Float, level: Int) {}
    override fun getExhaustion() = 0f
    override fun setExhaustion(value: Float) {}
    override fun getSaturation() = 0f
    override fun setSaturation(value: Float) {}
    override fun getFoodLevel() = 20
    override fun setFoodLevel(value: Int) {}
    override fun getSaturatedRegenRate() = 0
    override fun setSaturatedRegenRate(ticks: Int) {}
    override fun getUnsaturatedRegenRate() = 0
    override fun setUnsaturatedRegenRate(ticks: Int) {}
    override fun getStarvationRate() = 0
    override fun setStarvationRate(ticks: Int) {}
    override fun getLastDeathLocation() = null
    override fun setLastDeathLocation(location: org.bukkit.Location?) {}
    override fun getFireworkBoost() = throw NotImplementedError()
    override fun getPreviousGameMode() = null
    override fun setPreviousGameMode(mode: org.bukkit.GameMode?) {}
    override fun getSpectatorTargetCameraPosition() = throw NotImplementedError()
    override fun getPotionEffects(): MutableCollection<org.bukkit.potion.PotionEffect> = mutableListOf()
    override fun addPotionEffects(effects: MutableCollection<org.bukkit.potion.PotionEffect>) = true
    override fun getActivePotionEffects(): MutableCollection<org.bukkit.potion.PotionEffect> = mutableListOf()
    override fun hasPotionEffect(type: org.bukkit.potion.PotionEffectType) = false
    override fun getPotionEffect(type: org.bukkit.potion.PotionEffectType) = null
    override fun removePotionEffect(type: org.bukkit.potion.PotionEffectType) {}
    override fun getArrowCooldown() = 0
    override fun setArrowCooldown(ticks: Int) {}
    override fun getArrowsInBody() = 0
    override fun setArrowsInBody(count: Int) {}
    override fun getBeeStingerCooldown() = 0
    override fun setBeeStingerCooldown(ticks: Int) {}
    override fun getBeeStingersInBody() = 0
    override fun setBeeStingersInBody(count: Int) {}
    override fun getMaximumNoDamageTicks() = 20
    override fun setMaximumNoDamageTicks(ticks: Int) {}
    override fun getLastDamage() = 0.0
    override fun setLastDamage(damage: Double) {}
    override fun getNoActionTicks() = 0
    override fun setNoActionTicks(ticks: Int) {}
    override fun getRemainingAir() = 300
    override fun setRemainingAir(ticks: Int) {}
    override fun getMaximumAir() = 300
    override fun setMaximumAir(ticks: Int) {}
    override fun getArrowCooldown(hand: org.bukkit.inventory.EquipmentSlot) = 0
    override fun setArrowCooldown(hand: org.bukkit.inventory.EquipmentSlot, ticks: Int) {}
    override fun getFreezeTicks() = 0
    override fun setFreezeTicks(ticks: Int) {}
    override fun getMaxFreezeTicks() = 0
    override fun isFrozen() = false
    override fun isClimbing() = false
    override fun setVisualFire(visualFire: Boolean) {}
    override fun isVisualFire() = false
    override fun attack(target: org.bukkit.entity.Entity) {}
    override fun swingMainHand() {}
    override fun swingOffHand() {}
    override fun openInventory(inventory: org.bukkit.inventory.InventoryView) {}
    override fun openWorkbench(location: org.bukkit.Location?, force: Boolean) {}
    override fun openEnchanting(location: org.bukkit.Location?, force: Boolean) {}
    override fun openMerchant(trader: org.bukkit.entity.Villager, force: Boolean) {}
    override fun openMerchant(merchant: org.bukkit.inventory.Merchant, force: Boolean) {}
    override fun openSign(sign: org.bukkit.block.Sign) {}
    override fun awardAdvancement(advancement: org.bukkit.advancement.Advancement) {}
    override fun awardCriteria(advancement: org.bukkit.advancement.Advancement, criteria: String) {}
    override fun getAdvancementProgress(advancement: org.bukkit.advancement.Advancement) = throw NotImplementedError()
    override fun getAdvancements() = throw NotImplementedError()
    override fun incrementStatistic(statistic: org.bukkit.Statistic) {}
    override fun incrementStatistic(statistic: org.bukkit.Statistic, amount: Int) {}
    override fun incrementStatistic(statistic: org.bukkit.Statistic, material: Material) {}
    override fun incrementStatistic(statistic: org.bukkit.Statistic, material: Material, amount: Int) {}
    override fun incrementStatistic(statistic: org.bukkit.Statistic, entityType: org.bukkit.entity.EntityType) {}
    override fun incrementStatistic(statistic: org.bukkit.Statistic, entityType: org.bukkit.entity.EntityType, amount: Int) {}
    override fun decrementStatistic(statistic: org.bukkit.Statistic) {}
    override fun decrementStatistic(statistic: org.bukkit.Statistic, amount: Int) {}
    override fun decrementStatistic(statistic: org.bukkit.Statistic, material: Material) {}
    override fun decrementStatistic(statistic: org.bukkit.Statistic, entityType: org.bukkit.entity.EntityType) {}
    override fun decrementStatistic(statistic: org.bukkit.Statistic, material: Material, amount: Int) {}
    override fun decrementStatistic(statistic: org.bukkit.Statistic, entityType: org.bukkit.entity.EntityType, amount: Int) {}
    override fun setStatistic(statistic: org.bukkit.Statistic, value: Int) {}
    override fun setStatistic(statistic: org.bukkit.Statistic, material: Material, value: Int) {}
    override fun setStatistic(statistic: org.bukkit.Statistic, entityType: org.bukkit.entity.EntityType, newValue: Int) {}
    override fun getStatistic(statistic: org.bukkit.Statistic) = 0
    override fun getStatistic(statistic: org.bukkit.Statistic, material: Material) = 0
    override fun getStatistic(statistic: org.bukkit.Statistic, entityType: org.bukkit.entity.EntityType) = 0
    override fun updateInventory() {}
    override fun setWindowProperty(prop: org.bukkit.inventory.InventoryView.Property, value: Int) {}
    override fun getOpenInventory() = null
    override fun getItemOnCursor() = null
    override fun setItemOnCursor(item: ItemStack?) {}
    override fun hasCooldown(material: Material) = false
    override fun getCooldownPeriod(material: Material) = 0
    override fun setCooldown(material: Material, ticks: Int) {}
    override fun getGameMode() = throw NotImplementedError()
    override fun setGameMode(mode: org.bukkit.GameMode) {}
    override fun isBlocking() = false
    override fun getSleepTicks() = 0
    override fun getLocation(p0: org.bukkit.Location?) = throw NotImplementedError()
    override fun getAffectsSpawning() = true
    override fun setAffectsSpawning(affects: Boolean) {}
    override fun getViewDistance() = 10
    override fun setViewDistance(viewDistance: Int) {}
    override fun getSimulationDistance() = 10
    override fun setSimulationDistance(simulationDistance: Int) {}
    override fun getNoPhysicsDistance() = 10
    override fun setNoPhysicsDistance(simulationDistance: Int) {}
    override fun sendSignChange(location: org.bukkit.Location, lines: Array<out String>?) {}
    override fun sendSignChange(location: org.bukkit.Location, lines: Array<out String>?, dyeColor: org.bukkit.DyeColor?) {}
    override fun sendSignChange(location: org.bukkit.Location, lines: Array<out String>?, dyeColor: org.bukkit.DyeColor?, hasGlowingText: Boolean) {}
    override fun sendBlockDamage(loc: org.bukkit.Location, progress: Float) {}
    override fun sendBlockDamage(loc: org.bukkit.Location, progress: Float, entityId: Int) {}
    override fun sendBlockDamage(loc: org.bukkit.Location, progress: Float, source: org.bukkit.entity.Entity?) {}
    override fun sendMultiBlockChange(blocks: MutableMap<out org.bukkit.Location, org.bukkit.block.data.BlockData>?) {}
    override fun sendMultiBlockChange(blocks: MutableMap<out org.bukkit.Location, org.bukkit.block.data.BlockData>?, suppressLightUpdates: Boolean) {}
    override fun sendBlockChanges(blocks: MutableCollection<org.bukkit.block.data.BlockData>?) {}
    override fun sendBlockChanges(blocks: MutableCollection<org.bukkit.block.data.BlockData>?, suppressLightUpdates: Boolean) {}
    override fun sendEquipmentChange(livingEntity: org.bukkit.entity.LivingEntity, equipmentSlot: org.bukkit.inventory.EquipmentSlot, itemStack: ItemStack) {}
    override fun sendEquipmentChange(livingEntity: org.bukkit.entity.LivingEntity, slots: MutableMap<org.bukkit.inventory.EquipmentSlot, ItemStack>) {}
    override fun getWorldUID() = throw NotImplementedError()
    override fun getGameProfile() = throw NotImplementedError()
    override fun spigot() = throw NotImplementedError()
    override fun getChunk(x: Int, z: Int, gen: Boolean) = throw NotImplementedError()
    override fun playEffect(location: org.bukkit.Location, effect: org.bukkit.Effect, data: Int) {}
    override fun sendBlockChange(location: org.bukkit.Location, material: Material, data: Byte) {}
    override fun sendMap(mapView: org.bukkit.map.MapView?) {}
    override fun playNote(location: org.bukkit.Location, instrument: Byte, note: Byte) {}
    override fun playSound(location: org.bukkit.Location, sound: net.kyori.adventure.sound.Sound) {}
    override fun playSound(location: org.bukkit.Location, sound: net.kyori.adventure.sound.Sound, emitter: net.kyori.adventure.sound.Sound.Emitter) {}
    override fun playSound(entity: org.bukkit.entity.Entity, sound: net.kyori.adventure.sound.Sound) {}
    override fun playSound(entity: org.bukkit.entity.Entity, sound: net.kyori.adventure.sound.Sound, emitter: net.kyori.adventure.sound.Sound.Emitter) {}
    override fun stopSound(sound: net.kyori.adventure.sound.Sound) {}
    override fun stopSound(sound: net.kyori.adventure.sound.Sound, emitter: net.kyori.adventure.sound.Sound.Emitter) {}
    override fun stopSound(sound: org.bukkit.Sound) {}
    override fun stopSound(sound: String) {}
    override fun stopSound(sound: org.bukkit.Sound, category: org.bukkit.SoundCategory?) {}
    override fun stopSound(category: org.bukkit.SoundCategory?) {}
    override fun stopSound(category: org.bukkit.SoundCategory?, sound: String) {}
    override fun playSound(location: org.bukkit.Location, sound: String, volume: Float, pitch: Float) {}
    override fun playSound(location: org.bukkit.Location, sound: org.bukkit.Sound, volume: Float, pitch: Float) {}
    override fun playSound(location: org.bukkit.Location, sound: org.bukkit.Sound, category: org.bukkit.SoundCategory?, volume: Float, pitch: Float) {}
    override fun getTargetBlockExact(maxDistance: Int) = throw NotImplementedError()
    override fun getTargetBlockExact(maxDistance: Int, fluidCollisionMode: org.bukkit.FluidCollisionMode) = throw NotImplementedError()
    override fun getLastTwoTargetBlocksExact(maxDistance: Int, fluidCollisionMode: org.bukkit.FluidCollisionMode) = throw NotImplementedError()
    override fun getLineOfSight(transparent: MutableSet<Material>?, maxDistance: Int) = throw NotImplementedError()
    override fun getTargetBlock(transparent: MutableSet<Material>?, maxDistance: Int) = throw NotImplementedError()
    override fun getLastTwoTargetBlocks(transparent: MutableSet<Material>?, maxDistance: Int) = throw NotImplementedError()
    override fun getEyeHeight() = 1.62
    override fun getEyeHeight(ignorePose: Boolean) = 1.62
    override fun getEyeLocation() = throw NotImplementedError()
    override fun getTargetEntity(maxDistance: Int, ignoreBlocks: Boolean) = throw NotImplementedError()
    override fun performCommand(command: String) = true
    override fun isSneaking() = false
    override fun setSneaking(sneak: Boolean) {}
    override fun isSprinting() = false
    override fun setSprinting(sprinting: Boolean) {}
    override fun saveData() {}
    override fun loadData() {}
    override fun setSleepingIgnored(isSleeping: Boolean) {}
    override fun isSleepingIgnored() = false
    override fun getBedLocation() = null
    override fun getMainHand() = throw NotImplementedError()
    override fun sendResourcePacks(vararg resourcePacks: org.bukkit.profile.PlayerResourcePack?) {}
    override fun getResourcePacks(): MutableList<org.bukkit.profile.PlayerResourcePack> = mutableListOf()
    override fun addResourcePack(resourcePack: org.bukkit.profile.PlayerResourcePack) {}
    override fun removeResourcePack(uuid: UUID) {}
    override fun removeResourcePacks() {}
    override fun getPlayerProfile(): org.bukkit.profile.PlayerProfile = throw NotImplementedError()
    override fun setPlayerProfile(profile: org.bukkit.profile.PlayerProfile?) {}
    override fun getCooldownPeriod(net: net.kyori.adventure.key.Key?) = 0
    override fun hasCooldown(net: net.kyori.adventure.key.Key?) = false
    override fun setCooldown(net: net.kyori.adventure.key.Key?, ticks: Int) {}
    override fun isConversing() = false
    override fun acceptConversationInput(input: String?) = false
    override fun beginConversation(conversation: org.bukkit.conversations.Conversation?) = false
    override fun abandonConversation(conversation: org.bukkit.conversations.Conversation?) {}
    override fun abandonConversation(conversation: org.bukkit.conversations.Conversation?, details: org.bukkit.conversations.ConversationAbandonedEvent?) {}
    override fun sendRawMessage(message: String?) {}
    override fun sendRawMessage(sender: UUID?, message: String?) {}
    override fun kickPlayer(message: String?) {}
    override fun kick() {}
    override fun kick(message: net.kyori.adventure.text.Component?) {}
    override fun chat(msg: String) {}
    override fun getDisplayName() = net.kyori.adventure.text.Component.text("TestPlayer")
    override fun setDisplayName(name: net.kyori.adventure.text.Component?) {}
    override fun getPlayerListName(net: net.kyori.adventure.text.Component?) = net.kyori.adventure.text.Component.text("TestPlayer")
    override fun setPlayerListName(name: net.kyori.adventure.text.Component?) {}
    override fun setPlayerListHeader(header: net.kyori.adventure.text.Component?) {}
    override fun setPlayerListFooter(footer: net.kyori.adventure.text.Component?) {}
    override fun getPlayerListHeader(net: net.kyori.adventure.text.Component?) = null
    override fun getPlayerListFooter(net: net.kyori.adventure.text.Component?) = null
    override fun getCustomName() = null
    override fun setCustomName(name: net.kyori.adventure.text.Component?) {}
    override fun addAttachment(plugin: org.bukkit.plugin.Plugin) = throw NotImplementedError()
    override fun addAttachment(plugin: org.bukkit.plugin.Plugin, ticks: Int) = throw NotImplementedError()
    override fun addAttachment(plugin: org.bukkit.plugin.Plugin, name: String, value: Boolean) = throw NotImplementedError()
    override fun addAttachment(plugin: org.bukkit.plugin.Plugin, name: String, value: Boolean, ticks: Int) = throw NotImplementedError()
    override fun removeAttachment(attachment: org.bukkit.permissions.PermissionAttachment) {}
    override fun recalculatePermissions() {}
    override fun getEffectivePermissions(permissions: MutableSet<org.bukkit.permissions.Permission>?): MutableSet<org.bukkit.permissions.PermissionAttachmentInfo> = mutableSetOf()
    override fun isPermissionSet(name: String) = false
    override fun isPermissionSet(perm: org.bukkit.permissions.Permission) = false
    override fun hasPermission(name: String) = true
    override fun hasPermission(perm: org.bukkit.permissions.Permission) = true
    override fun getNearbyEntities(x: Double, y: Double, z: Double) = throw NotImplementedError()
    override fun setMetadata(metadataKey: String, newMetadataValue: org.bukkit.metadata.MetadataValue?) {}
    override fun getMetadata(metadataKey: String): MutableList<org.bukkit.metadata.MetadataValue> = mutableListOf()
    override fun hasMetadata(metadataKey: String) = false
    override fun removeMetadata(metadataKey: String, owningPlugin: org.bukkit.plugin.Plugin) {}
    override fun <T : org.bukkit.plugin.messaging.PluginMessageRecipient> T.get(): T = this as T
    override fun sendPluginMessage(source: org.bukkit.plugin.Plugin, channel: String, message: ByteArray) {}
    override fun getPassengers(): MutableList<org.bukkit.entity.Entity> = mutableListOf()
    override fun addPassenger(passenger: org.bukkit.entity.Entity) = false
    override fun removePassenger(passenger: org.bukkit.entity.Entity) = false
    override fun eject() = false
    override fun getVelocity() = throw NotImplementedError()
    override fun setVelocity(velocity: org.bukkit.util.Vector) {}
    override fun getHeight() = 1.8
    override fun getWidth() = 0.6
    override fun getBoundingBox() = throw NotImplementedError()
    override fun isOnGround() = true
    override fun isInWater() = false
    override fun isInLava() = false
    override fun isInRain() = false
    override fun isInBubbleColumn() = false
    override fun isInPowderedSnow() = false
    override fun getEnchantmentSeed() = 0
    override fun setEnchantmentSeed(seed: Int) {}
    override fun getHandle() = throw NotImplementedError()
    override fun getFacing() = throw NotImplementedError()
    override fun getPortalCooldown(hand: org.bukkit.inventory.EquipmentSlot) = 0
    override fun setPortalCooldown(hand: org.bukkit.inventory.EquipmentSlot, cooldown: Int) {}
    override fun getHandRaised(hand: org.bukkit.inventory.EquipmentSlot) = throw NotImplementedError()
    override fun isHandRaised(hand: org.bukkit.inventory.EquipmentSlot) = false
    override fun getAttackable() = true
    override fun setAttackable(attackable: Boolean) {}
    override fun isInWorld() = true
    override fun getOrigin(): org.bukkit.Location = throw NotImplementedError()
    override fun fromOrigin(): Boolean = false

    @Deprecated("Deprecated in Java")
    override fun resetPlayerWeather() {}
    @Deprecated("Deprecated in Java")
    override fun getPlayerTimeOffset(): Long = 0
    @Deprecated("Deprecated in Java")
    override fun isPlayerTimeRelative(): Boolean = false

    @Deprecated("Deprecated in Java")
    override fun <T : Any?> getWorldBorder(): org.bukkit.WorldBorder = throw NotImplementedError()
}
