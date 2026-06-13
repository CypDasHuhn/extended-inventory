package dev.cypdashuhn.extendedinventory.commands

import dev.cypdashuhn.extendedinventory.actions.AnchorActions
import dev.cypdashuhn.extendedinventory.actions.CycleActions
import dev.cypdashuhn.extendedinventory.actions.ProfileActions
import dev.cypdashuhn.extendedinventory.actions.isValidResourceName
import dev.cypdashuhn.extendedinventory.db.PlayerProfileStatus
import dev.cypdashuhn.extendedinventory.db.ProfileManager
import dev.cypdashuhn.extendedinventory.db.ProfileOpenness
import dev.cypdashuhn.extendedinventory.hotbar.HotbarManager
import dev.cypdashuhn.extendedinventory.hotbar.HotbarMode
import dev.cypdashuhn.extendedinventory.ui.inventory.InventoryInterface
import dev.cypdashuhn.extendedinventory.ui.inventory.InventoryInterfaceContext
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.arguments.PlayerArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.PlayerCommandExecutor

fun ex() {
    CommandTree("ex")
        .executesPlayer(PlayerCommandExecutor { sender, _ ->
            val state = HotbarManager.getState(sender)
            val profileId = HotbarManager.ensureProfile(sender)
            InventoryInterface.openInventory(sender, InventoryInterfaceContext(profileId, state.x, state.y))
        })
        .then(buildJumpToNode())
        .then(buildCurrentPositionNode())
        .then(buildModeNode())
        .then(buildUpNode())
        .then(buildDownNode())
        .then(buildLeftNode())
        .then(buildRightNode())
        .then(buildCycleNode())
        .then(buildProfileNodes())
        .then(buildBufferNode())
        .then(buildAnchorNodes())
        .register()
}

private fun buildJumpToNode() = la("jumpto").apply {
    then(IntegerArgument("x").apply {
        then(IntegerArgument("y").apply {
            executesPlayer(PlayerCommandExecutor { sender, args ->
                val x = args.get("x") as Int
                val y = args.get("y") as Int
                HotbarManager.jumpTo(sender, x, y)
                sender.msg("<green>Jumped to ($x, $y).")
            })
        })
    })
    then(StringArgument("anchorName").suggestAnchorNames().apply {
        executesPlayer(PlayerCommandExecutor { sender, args ->
            val target = args.get("anchorName") as String
            val profileId = HotbarManager.ensureProfile(sender)
            val anchor = AnchorActions.getAnchorInfo(profileId, target)
            if (anchor != null) {
                HotbarManager.jumpTo(sender, anchor.x, anchor.y)
                sender.msg("<green>Jumped to anchor '<white>$target</white>' at (${anchor.x}, ${anchor.y}).")
            } else {
                sender.msg("<red>Anchor '<white>$target</white>' not found.")
            }
        })
    })
    executesPlayer(PlayerCommandExecutor { sender, _ ->
        sender.msg("<yellow>Usage: /ex jumpto <x> <y> or /ex jumpto <anchor-name>")
    })
}

private fun buildCurrentPositionNode() = la("current-position").apply {
    executesPlayer(PlayerCommandExecutor { sender, _ ->
        val state = HotbarManager.getState(sender)
        sender.msg("<green>Current position: (<white>${state.x}</white>, <white>${state.y}</white>)")
    })
}

private fun buildModeNode() = la("mode").apply {
    then(la("anchored").apply {
        executesPlayer(PlayerCommandExecutor { sender, _ ->
            val state = HotbarManager.getState(sender)
            state.anchored = true
            state.mode = HotbarMode.LOCKED
            sender.msg("<green>Mode set to <white>anchored</white>. Navigation locked, hotbar items locked.")
        })
    })
    then(la("free").apply {
        executesPlayer(PlayerCommandExecutor { sender, _ ->
            val state = HotbarManager.getState(sender)
            state.anchored = false
            state.mode = HotbarMode.FREE
            sender.msg("<green>Mode set to <white>free</white>. Navigation and hotbar unlocked.")
        })
    })
    executesPlayer(PlayerCommandExecutor { sender, _ ->
        val state = HotbarManager.getState(sender)
        sender.msg("<green>Anchored: <white>${state.anchored}</white>, Mode: <white>${state.mode}</white>")
    })
}

private fun buildUpNode() = la("up").apply {
    then(IntegerArgument("amount").setOptional(true).apply {
        executesPlayer(PlayerCommandExecutor { sender, args ->
            val amount = (args.get("amount") as? Int) ?: 1
            repeat(amount) { HotbarManager.navigate(sender, 0, -1) }
            val state = HotbarManager.getState(sender)
            sender.msg("<green>Position: (<white>${state.x}</white>, <white>${state.y}</white>)")
        })
    })
    executesPlayer(PlayerCommandExecutor { sender, _ ->
        HotbarManager.navigate(sender, 0, -1)
        val state = HotbarManager.getState(sender)
        sender.msg("<green>Position: (<white>${state.x}</white>, <white>${state.y}</white>)")
    })
}

private fun buildDownNode() = la("down").apply {
    then(IntegerArgument("amount").setOptional(true).apply {
        executesPlayer(PlayerCommandExecutor { sender, args ->
            val amount = (args.get("amount") as? Int) ?: 1
            repeat(amount) { HotbarManager.navigate(sender, 0, 1) }
            val state = HotbarManager.getState(sender)
            sender.msg("<green>Position: (<white>${state.x}</white>, <white>${state.y}</white>)")
        })
    })
    executesPlayer(PlayerCommandExecutor { sender, _ ->
        HotbarManager.navigate(sender, 0, 1)
        val state = HotbarManager.getState(sender)
        sender.msg("<green>Position: (<white>${state.x}</white>, <white>${state.y}</white>)")
    })
}

private fun buildLeftNode() = la("left").apply {
    then(IntegerArgument("amount").setOptional(true).apply {
        executesPlayer(PlayerCommandExecutor { sender, args ->
            val amount = (args.get("amount") as? Int) ?: 1
            repeat(amount) { HotbarManager.navigate(sender, -1, 0) }
            val state = HotbarManager.getState(sender)
            sender.msg("<green>Position: (<white>${state.x}</white>, <white>${state.y}</white>)")
        })
    })
    executesPlayer(PlayerCommandExecutor { sender, _ ->
        HotbarManager.navigate(sender, -1, 0)
        val state = HotbarManager.getState(sender)
        sender.msg("<green>Position: (<white>${state.x}</white>, <white>${state.y}</white>)")
    })
}

private fun buildRightNode() = la("right").apply {
    then(IntegerArgument("amount").setOptional(true).apply {
        executesPlayer(PlayerCommandExecutor { sender, args ->
            val amount = (args.get("amount") as? Int) ?: 1
            repeat(amount) { HotbarManager.navigate(sender, 1, 0) }
            val state = HotbarManager.getState(sender)
            sender.msg("<green>Position: (<white>${state.x}</white>, <white>${state.y}</white>)")
        })
    })
    executesPlayer(PlayerCommandExecutor { sender, _ ->
        HotbarManager.navigate(sender, 1, 0)
        val state = HotbarManager.getState(sender)
        sender.msg("<green>Position: (<white>${state.x}</white>, <white>${state.y}</white>)")
    })
}

private fun buildCycleNode() = la("cycle").apply {
    executesPlayer(PlayerCommandExecutor { sender, _ ->
        val state = HotbarManager.getState(sender)
        val profileId = state.profileId ?: return@PlayerCommandExecutor
        val positions = CycleActions.getCyclePositions(profileId, state.x, state.y)
        if (positions.isEmpty()) {
            sender.msg("<red>No other positions with this material.")
            return@PlayerCommandExecutor
        }
        val next = positions.first()
        HotbarManager.jumpTo(sender, next.first, next.second)
        sender.msg("<green>Cycled to next position: (<white>${next.first}</white>, <white>${next.second}</white>)")
    })
}

private fun buildProfileNodes() = la("profiles").apply {
    then(la("switch").apply {
        then(StringArgument("name").suggestProfileNames().apply {
            executesPlayer(PlayerCommandExecutor { sender, args ->
                val name = args.get("name") as String
                val profile = ProfileManager.findByName(name)
                if (profile != null) {
                    ProfileActions.switchProfile(sender, profile.id.value)
                    HotbarManager.getState(sender).profileId = profile.id.value
                    HotbarManager.mirrorToHotbar(sender)
                    sender.msg("<green>Switched to profile '<white>$name</white>'.")
                } else {
                    if (isValidResourceName(name)) {
                        val id = ProfileActions.createProfile(sender, name)
                        HotbarManager.getState(sender).profileId = id
                        HotbarManager.mirrorToHotbar(sender)
                        sender.msg("<green>Created and switched to profile '<white>$name</white>'.")
                    } else {
                        sender.msg("<red>Invalid profile name.")
                    }
                }
            })
        })
    })
    then(la("set-default").apply {
        then(StringArgument("name").suggestProfileNames().apply {
            executesPlayer(PlayerCommandExecutor { sender, args ->
                val name = args.get("name") as String
                val profile = ProfileManager.findByName(name)
                if (profile != null) {
                    ProfileActions.setDefault(sender, profile.id.value)
                    sender.msg("<green>Set '<white>$name</white>' as default profile.")
                } else {
                    sender.msg("<red>Profile '<white>$name</white>' not found.")
                }
            })
        })
    })
    then(la("subscribe").apply {
        then(StringArgument("name").suggestProfileNames().apply {
            executesPlayer(PlayerCommandExecutor { sender, args ->
                val name = args.get("name") as String
                val profile = ProfileManager.findByName(name)
                if (profile != null) {
                    ProfileActions.subscribe(sender, profile.id.value)
                    sender.msg("<green>Subscribed to '<white>$name</white>'.")
                } else {
                    sender.msg("<red>Profile '<white>$name</white>' not found.")
                }
            })
        })
    })
    then(la("invite").apply {
        then(StringArgument("profileName").suggestProfileNames().apply {
            then(PlayerArgument("targetPlayer").apply {
                then(la("read-only").apply {
                    executesPlayer(PlayerCommandExecutor { sender, args ->
                        handleInvite(sender, args, PlayerProfileStatus.READ_ONLY)
                    })
                })
                then(la("full").apply {
                    executesPlayer(PlayerCommandExecutor { sender, args ->
                        handleInvite(sender, args, PlayerProfileStatus.WRITE_READ)
                    })
                })
            })
        })
    })
    then(la("settings").apply {
        then(StringArgument("name").suggestProfileNames().apply {
            then(la("turn-private").apply {
                executesPlayer(PlayerCommandExecutor { sender, args ->
                    handleSettings(sender, args, ProfileOpenness.PRIVATE)
                })
            })
            then(la("turn-public-read").apply {
                executesPlayer(PlayerCommandExecutor { sender, args ->
                    handleSettings(sender, args, ProfileOpenness.PUBLIC_READ)
                })
            })
            then(la("turn-public-write").apply {
                executesPlayer(PlayerCommandExecutor { sender, args ->
                    handleSettings(sender, args, ProfileOpenness.PUBLIC_WRITE)
                })
            })
        })
    })
    executesPlayer(PlayerCommandExecutor { sender, _ ->
        sender.msg("<yellow>Usage: /ex profiles <switch|set-default|subscribe|invite|settings>")
    })
}

private fun buildBufferNode() = la("buffer").apply {
    then(la("load").apply {
        then(StringArgument("name").setOptional(true).suggestBufferNames().apply {
            executesPlayer(PlayerCommandExecutor { sender, args ->
                val name = args.get("name") as? String
                val success = HotbarManager.loadBuffer(sender, name)
                if (success) {
                    sender.msg("<green>Buffer loaded.")
                } else {
                    sender.msg("<red>No buffer found.")
                }
            })
        })
    })
}

private fun buildAnchorNodes() = la("anchor").apply {
    then(la("add").apply {
        then(StringArgument("name").apply {
            then(IntegerArgument("x").apply {
                then(IntegerArgument("y").apply {
                    executesPlayer(PlayerCommandExecutor { sender, args ->
                        val name = args.get("name") as String
                        val x = args.get("x") as Int
                        val y = args.get("y") as Int
                        val profileId = HotbarManager.ensureProfile(sender)
                        AnchorActions.addAnchor(profileId, name, x, y)
                        sender.msg("<green>Anchor '<white>$name</white>' added at ($x, $y).")
                    })
                })
            })
        })
    })
    then(la("delete").apply {
        then(StringArgument("name").suggestAnchorNames().apply {
            executesPlayer(PlayerCommandExecutor { sender, args ->
                val name = args.get("name") as String
                val profileId = HotbarManager.ensureProfile(sender)
                val success = AnchorActions.deleteAnchor(sender, profileId, name)
                if (success) {
                    sender.msg("<green>Anchor '<white>$name</white>' deleted.")
                } else {
                    sender.msg("<red>Cannot delete anchor '<white>$name</white>'.")
                }
            })
        })
    })
    then(la("rename").apply {
        then(StringArgument("oldName").suggestAnchorNames().apply {
            then(StringArgument("newName").apply {
                executesPlayer(PlayerCommandExecutor { sender, args ->
                    val oldName = args.get("oldName") as String
                    val newName = args.get("newName") as String
                    val profileId = HotbarManager.ensureProfile(sender)
                    val success = AnchorActions.renameAnchor(sender, profileId, oldName, newName)
                    if (success) {
                        sender.msg("<green>Anchor renamed to '<white>$newName</white>'.")
                    } else {
                        sender.msg("<red>Cannot rename anchor.")
                    }
                })
            })
        })
    })
    then(la("info").apply {
        then(StringArgument("name").suggestAnchorNames().apply {
            executesPlayer(PlayerCommandExecutor { sender, args ->
                val name = args.get("name") as String
                val profileId = HotbarManager.ensureProfile(sender)
                val anchor = AnchorActions.getAnchorInfo(profileId, name)
                if (anchor != null) {
                    sender.msg("<green>Anchor '<white>${anchor.name}</white>': position (${anchor.x}, ${anchor.y})")
                } else {
                    sender.msg("<red>Anchor '<white>$name</white>' not found.")
                }
            })
        })
    })
}

private fun handleInvite(sender: org.bukkit.entity.Player, args: dev.jorel.commandapi.executors.CommandArguments, status: PlayerProfileStatus) {
    val profileName = args.get("profileName") as String
    val targetPlayer = args.get("targetPlayer") as org.bukkit.entity.Player
    val profile = ProfileManager.findByName(profileName) ?: run {
        sender.msg("<red>Profile not found.")
        return
    }
    ProfileActions.invite(sender, profile.id.value, targetPlayer, status)
    val accessLabel = if (status == PlayerProfileStatus.READ_ONLY) "read-only" else "full"
    sender.msg("<green>Invited ${targetPlayer.name} to '<white>$profileName</white>' with $accessLabel access.")
}

private fun handleSettings(sender: org.bukkit.entity.Player, args: dev.jorel.commandapi.executors.CommandArguments, openness: ProfileOpenness) {
    val name = args.get("name") as String
    val profile = ProfileManager.findByName(name) ?: run {
        sender.msg("<red>Profile not found.")
        return
    }
    ProfileActions.setOpenness(profile.id.value, openness)
    val label = when (openness) {
        ProfileOpenness.PRIVATE -> "private"
        ProfileOpenness.PUBLIC_READ -> "public (read-only)"
        ProfileOpenness.PUBLIC_WRITE -> "public (write access)"
    }
    sender.msg("<green>Profile '<white>$name</white>' is now $label.")
}
