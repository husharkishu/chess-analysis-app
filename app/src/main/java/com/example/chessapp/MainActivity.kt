package com.example.chessapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.pgn.PgnHolder
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var board: Board
    private lateinit var chessBoardView: ChessBoardView
    private lateinit var pgnInput: TextInputEditText
    private lateinit var loadPgnButton: Button
    private lateinit var loadFileButton: Button // We will add logic for this later
    private lateinit var prevMoveButton: Button
    private lateinit var nextMoveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the board and view
        board = Board()
        chessBoardView = ChessBoardView(this)

        // Find the placeholder in the layout and add our new view to it
        val placeholder = findViewById<FrameLayout>(R.id.chessboard_placeholder)
        placeholder.addView(chessBoardView)

        // Find all the other UI elements
        pgnInput = findViewById(R.id.text_input_pgn)
        loadPgnButton = findViewById(R.id.btn_load_pgn)
        loadFileButton = findViewById(R.id.btn_load_file)
        prevMoveButton = findViewById(R.id.btn_prev_move)
        nextMoveButton = findViewById(R.id.btn_next_move)

        // Set up button click listeners
        loadPgnButton.setOnClickListener {
            loadPgnOrFen(pgnInput.text.toString())
        }

        nextMoveButton.setOnClickListener {
            if (board.moveHistory.size > board.halfMoveCounter) {
                // Use the built-in doMove method from chesslib
                board.doMove(board.moveHistory[board.halfMoveCounter])
                chessBoardView.setBoard(board)
            }
        }

        prevMoveButton.setOnClickListener {
            if (board.halfMoveCounter > 0) {
                board.undoMove()
                chessBoardView.setBoard(board)
            }
        }
        
        // Load the starting position
        loadPgnOrFen(Board.DEFAULT_STARTING_FEN)
    }

    private fun loadPgnOrFen(text: String) {
        if (text.isBlank()) {
            Toast.makeText(this, "Input is empty", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Try loading as FEN first
            if (text.contains("/") || !text.contains(".")) {
                board.loadFen(text)
            } else {
                // Try loading as PGN
                val pgn = PgnHolder("temp.pgn")
                pgn.loadPgn(text)
                // Go to the start of the game
                pgn.getGames().firstOrNull()?.let { game ->
                    // --- THIS IS THE FIX ---
                    // The 'game' object *is* the board, not 'game.board'
                    board = game
                    game.gotoMove(0) // Go to the very first move
                    // --- END OF FIX ---
                } ?: run {
                    board.loadFen(Board.DEFAULT_STARTING_FEN) // Fallback
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error parsing PGN/FEN: ${e.message}", Toast.LENGTH_LONG).show()
            board.loadFen(Board.DEFAULT_STARTING_FEN) // Reset on error
        }

        // Display the new board
        chessBoardView.setBoard(board)
    }
}
