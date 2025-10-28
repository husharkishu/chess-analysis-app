package com.example.chessapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Square

class ChessBoardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val lightPaint = Paint().apply { color = Color.parseColor("#F0D9B5") }
    private val darkPaint = Paint().apply { color = Color.parseColor("#B58863") }
    private val piecePaint = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private var board: Board = Board()
    private var squareSize = 0f

    init {
        // Set a default board so the view isn't empty
        board.loadFen(Board.DEFAULT_STARTING_FEN)
    }

    fun setBoard(newBoard: Board) {
        this.board = newBoard
        invalidate() // Tell the view to redraw itself
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Make the board a square
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = if (width < height) width else height
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        squareSize = w / 8f
        // Adjust text size based on square size for the pieces
        piecePaint.textSize = squareSize * 0.75f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawSquares(canvas)
        drawPieces(canvas)
    }

    private fun drawSquares(canvas: Canvas) {
        for (row in 0..7) {
            for (col in 0..7) {
                val paint = if ((row + col) % 2 == 0) lightPaint else darkPaint
                val left = col * squareSize
                val top = row * squareSize
                canvas.drawRect(left, top, left + squareSize, top + squareSize, paint)
            }
        }
    }

    private fun drawPieces(canvas: Canvas) {
        val textOffset = (piecePaint.descent() + piecePaint.ascent()) / 2
        for (square in Square.values()) {
            if (square == Square.NONE) continue
            val piece = board.getPiece(square)
            if (piece != Piece.NONE) {
                // Board is drawn from top-left (A8), but FEN is read from A8.
                // We need to map the square to our drawing coordinates.
                val row = 7 - square.rank.ordinal // Invert row for drawing (0=rank 8)
                val col = square.file.ordinal      // 0=file A

                val x = (col * squareSize) + (squareSize / 2)
                val y = (row * squareSize) + (squareSize / 2) - textOffset

                canvas.drawText(getPieceUnicode(piece), x, y, piecePaint)
            }
        }
    }

    private fun getPieceUnicode(piece: Piece): String {
        return when (piece) {
            Piece.WHITE_PAWN -> "♙"
            Piece.WHITE_KNIGHT -> "♘"
            Piece.WHITE_BISHOP -> "♗"
            Piece.WHITE_ROOK -> "♖"
            Piece.WHITE_QUEEN -> "♕"
            Piece.WHITE_KING -> "♔"
            Piece.BLACK_PAWN -> "♟"
            Piece.BLACK_KNIGHT -> "♞"
            Piece.BLACK_BISHOP -> "♝"
            Piece.BLACK_ROOK -> "♜"
            Piece.BLACK_QUEEN -> "♛"
            Piece.BLACK_KING -> "♚"
            else -> ""
        }
    }
}
