package Players.AJR2546;

import Interface.PlayerMove;

/**
 * Created by Andres on 4/15/2014.
 */
public class BoardUtils {
    // Constants
    public static final int BOARD_SIZE = 4;
    public static final int NUM_STACKS = 3;
    public static final int NUM_PLAYERS = 2;

    public static void updateBoard(PlayerMove playerMove, StackP[][] board, Player[] players){
        //System.out.println("Updating board");

        // Making a new play from stack
        if(playerMove.getStartRow() == -1){

            int pID = playerMove.getPlayerId();
            int stack = playerMove.getStack();

            int eRow = playerMove.getEndRow();
            int eCol = playerMove.getEndCol();


            Piece p = players[pID-1].peekAtStacks()[stack-1];

            board[eRow][eCol].push(p);
        }else{

            int sRow = playerMove.getStartRow();
            int sCol = playerMove.getStartCol();

            Piece p = board[sRow][sCol].pop();

            int eRow = playerMove.getEndRow();
            int eCol = playerMove.getEndCol();

            board[eRow][eCol].push(p);
        }
    }

    /**
     * Validates the move based on the goblet rules
     *
     * @param move - The move to be validated
     * @return True if valid, False otherwise
     */
    public static boolean validateMove(PlayerMove move, StackP[][] board, Player[] players){
        // Validate the move structure for the starting points
        if(move.getStack() != 0){
            //Make sure it's built properly
            int row = move.getStartRow();
            int col = move.getStartCol();

            // Validate the row/col
            if(row != -1 || col != -1){
                //System.out.println("Its a stack play, but I have a start row/col");
                System.out.println("R: " + row + " C: " + col);
                return false;
            }

            // Validate the piece size
            if(move.getSize() != players[move.getPlayerId()-1].getStackSize(move.getStack()-1)){
                //System.out.println("Its a stack play, but the stacks don't match");
                return false;
            }

            int eRow = move.getEndRow();
            int eCol = move.getEndCol();
            // Check if you are moving onto a goblet, then validate by the rules

            if(board[eRow][eCol].size() > 0){
                //System.out.println("-----------------I can't gobble a piece from the player stack");
                return false; //Can't cover if coming from stack
            }

        }else{
            // Make sure it's built properly
            int row = move.getStartRow();
            int col = move.getStartCol();

            if(row == -1 || col == -1){
                return false;
            }

            if(board[row][col].empty() || move.getSize() != board[row][col].peek().getSize()){
                return false;
            }

            int eRow = move.getEndRow();
            int eCol = move.getEndCol();
            // Check if you are moving onto a goblet, then validate by the rules
            if(!board[eRow][eCol].empty()){
                if(board[eRow][eCol].peek().getSize() >= board[row][col].peek().getSize()){
                    return false;
                }
            }

        }
        return true;
    }

    public static StackP[][] copyBoard(StackP[][] board){
        StackP[][] ret = new StackP[board.length][board[0].length];
        for(int row = 0; row < board.length; row++){
            for(int col = 0; col < board[row].length; col++){
                ret[row][col] = board[row][col].copy();
            }
        }
        return ret;
    }

}
