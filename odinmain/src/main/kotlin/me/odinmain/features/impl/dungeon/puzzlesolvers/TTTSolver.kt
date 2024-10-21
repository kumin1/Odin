package me.odinmain.features.impl.dungeon.puzzlesolvers

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.DungeonEvents
import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.utils.Vec2
import me.odinmain.utils.addRotationCoords
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.tiles.Rotations
import me.odinmain.utils.toAABB
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.init.Items
import net.minecraft.util.BlockPos
import kotlin.experimental.and

object TTTSolver {

    // currently just rendering the board, no actual solving

    private var board = Array(9) { index ->
        BoardSlot(
            State.Blank, BlockPos(0, 0, 0), index % 3, index / 3,
            when (index) {
                4 -> BoardPosition.Middle
                0, 2, 6, 8 -> BoardPosition.Corner
                else -> BoardPosition.Edge
            }
        )
    }

    private data class BoardSlot(val state: State, val location: BlockPos, val row: Int, val column: Int, val position: BoardPosition)

    private var toRender: BlockPos? = null

    fun tttRoomEnter(event: DungeonEvents.RoomEnterEvent) {
        val room = event.fullRoom?.room ?: return
        if (room.data.name != "Tic Tac Toe") return

        updateBoard(room.vec2.addRotationCoords(room.rotation, 7, 0), room.rotation)
    }

    private fun updateBoard(bottomRight: Vec2, rotations: Rotations) {
        for (index in 0 until 9) {
            val currentSlot = bottomRight.addRotationCoords(rotations, 0, -index / 3).let { BlockPos(it.x.toDouble(), 70.0 + index % 3, it.z.toDouble())}
            board[index] = BoardSlot(findSlotState(currentSlot), currentSlot, index % 3, index / 3,
                when (index) {
                    4 -> BoardPosition.Middle
                    0, 2, 6, 8 -> BoardPosition.Corner
                    else -> BoardPosition.Edge
                })
        }
    }

    fun onMetaData(event: PostEntityMetadata) {
        val room = DungeonUtils.currentFullRoom?.room ?: return
        if (room.data.name != "Tic Tac Toe") return

        mc.theWorld?.getEntityByID(event.packet.entityId) as? EntityItemFrame ?: return
        updateBoard(room.vec2.addRotationCoords(room.rotation, 7, 0), room.rotation)
    }

    fun tttRenderWorld() {
        board.forEach { slot ->
            val color = when (slot.state) {
                State.X -> Color.RED
                State.O -> Color.BLUE
                else -> Color.WHITE
            }
            Renderer.drawBox(slot.location.toAABB(), color, 1f, fillAlpha = 0f)
        }
    }



    fun reset() {
        toRender = null
        board = Array(9) { index ->
            BoardSlot(
                State.Blank, BlockPos(0, 0, 0), index % 3, index / 3,
                when (index) {
                    4 -> BoardPosition.Middle
                    0, 2, 6, 8 -> BoardPosition.Corner
                    else -> BoardPosition.Edge
                }
            )
        }
    }

    private fun findSlotState(blockPos: BlockPos): State {
        val itemFrameBlock = mc.theWorld?.getEntitiesWithinAABB(EntityItemFrame::class.java, blockPos.toAABB())?.firstOrNull() ?: return State.Blank
        val mapData = Items.filled_map?.getMapData(itemFrameBlock.displayedItem, mc.theWorld) ?: return State.Blank
        return if ((mapData.colors[8256] and 255.toByte()).toInt() == 114) State.X else State.O
    }

    enum class State {
        Blank, X, O
    }

    enum class BoardPosition {
        Middle, Edge, Corner
    }
}