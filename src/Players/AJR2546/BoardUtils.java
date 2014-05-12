package Players.AJR2546;

import Interface.Coordinate;
import Interface.PlayerMove;

import java.util.ArrayList;
import java.util.HashMap;

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

        // Update the players
        if(playerMove.getStartRow() == -1){
            int pID = playerMove.getPlayerId();
            int stack = playerMove.getStack();

            players[pID-1].takeFromStack(stack-1);
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

    /**
     * Generates the list of all valid and possible moves in the board.
     *
     * From here we will choose what moves to use.
     * @return PlayerMove array of what's possible
     */
    public static PlayerMove[] generatePossibleMoves(StackP[][] board, Player[] players, int pID){

        // Get all the pieces that I can move
        HashMap<Coordinate, Piece> available = new HashMap<Coordinate, Piece>();


        // Find all the pieces at the top of the board
        for(int r = 0; r < BoardUtils.BOARD_SIZE; r++){
            for(int c = 0; c < BoardUtils.BOARD_SIZE; c++){
                if(!board[r][c].empty() && board[r][c].peek().getPlayerID() == pID){
                    available.put(new Coordinate(r,c), board[r][c].peek());
                }
            }
        }

        ArrayList<PlayerMove> intraBoardMoves = new ArrayList<PlayerMove>();

        // Generate the moves from all the available ones, valid and invalid
        for(int r = 0; r < BoardUtils.BOARD_SIZE; r++){
            for(int c = 0; c < BoardUtils.BOARD_SIZE; c++){

                for(Coordinate coord : available.keySet()){
                    // Dont move to the same place
                    if(!(coord.getRow() == r && coord.getCol() == c)){
                        Piece p = available.get(coord);
                        // Make new move moving from current location to r,c
                        intraBoardMoves.add(new PlayerMove(pID, 0, p.getSize(), coord, new Coordinate(r,c)));
                    }
                }

            }
        }

        // Store the valid moves for intra board things
        ArrayList<PlayerMove> validMoves = new ArrayList<PlayerMove>();

        // Validate all the moves generated
        for(PlayerMove i : intraBoardMoves){
            if(BoardUtils.validateMove(i,board,players)){
                validMoves.add(i);
            }
        }

        ArrayList<PlayerMove> stackMoves = new ArrayList<PlayerMove>();
        // Deal with the player stacks
        for(int r = 0; r < BoardUtils.BOARD_SIZE; r++){
            for(int c = 0; c < BoardUtils.BOARD_SIZE; c++){
                for(int s = 0; s < BoardUtils.NUM_STACKS; s++){

                    Piece p = players[pID-1].peekAtStacks()[s];

                    // If the stack is empty, continue
                    if(p == null){
                        continue;
                    }
                    stackMoves.add(new PlayerMove(pID, s+1, p.getSize(),
                            new Coordinate(-1,-1), new Coordinate(r,c)));
                }
            }
        }

        // Validate all these moves
        for(PlayerMove i : stackMoves){
            if(BoardUtils.validateMove(i,board,players))
                validMoves.add(i);
        }

        PlayerMove[] ret = new PlayerMove[validMoves.size()];

        validMoves.toArray(ret);

        return ret;
    }

    public static int checkWinner(StackP[][] tempBoard){
        //Check horizontal lines
        for(int row = 0; row < BoardUtils.BOARD_SIZE; row++){
            int pID = tempBoard[row][0].empty() ? -1 : tempBoard[row][0].peek().getPlayerID();
            boolean flag = false;
            for(int col = 1; col < BoardUtils.BOARD_SIZE; col++){
                if(tempBoard[row][col].empty() || tempBoard[row][col].peek().getPlayerID() != pID){
                    flag = true;
                    break;
                }
            }
            if(!flag){
                return pID;
            }
        }

        //Check for vertical lines
        for(int col = 0; col < BoardUtils.BOARD_SIZE; col++){
            int pID = tempBoard[0][col].empty() ? -1 : tempBoard[0][col].peek().getPlayerID();
            boolean flag = false;
            for(int row = 1; row < BoardUtils.BOARD_SIZE; row++){
                if(tempBoard[row][col].empty() || tempBoard[row][col].peek().getPlayerID() != pID){
                    flag = true;
                    break;
                }
            }
            if(!flag){
                return pID;
            }
        }


        int pId00 = tempBoard[0][0].empty() ? -1 : tempBoard[0][0].peek().getPlayerID();
        int pId11 = tempBoard[1][1].empty() ? -1 : tempBoard[1][1].peek().getPlayerID();
        int pId22 = tempBoard[2][2].empty() ? -1 : tempBoard[2][2].peek().getPlayerID();
        int pId33 = tempBoard[3][3].empty() ? -1 : tempBoard[3][3].peek().getPlayerID();

        if(pId00 == pId11 && pId11 == pId22 && pId22 == pId33){
            return pId00;
        }

        int pId03 = tempBoard[0][3].empty() ? -1 : tempBoard[0][3].peek().getPlayerID();
        int pId12 = tempBoard[1][2].empty() ? -1 : tempBoard[1][2].peek().getPlayerID();
        int pId21 = tempBoard[2][1].empty() ? -1 : tempBoard[2][1].peek().getPlayerID();
        int pId30 = tempBoard[3][0].empty() ? -1 : tempBoard[3][0].peek().getPlayerID();

        if(pId03 == pId12 && pId12 == pId21 && pId21 == pId30){
            return pId03;
        }


        // Check first diagonal
        int pID = tempBoard[0][0].empty() ? -1 : tempBoard[0][0].peek().getPlayerID();
        int i = 1;
        boolean flag = false;
        while(i < BoardUtils.BOARD_SIZE){
            if(tempBoard[i][i].empty() || tempBoard[i][i].peek().getPlayerID() != pID){
                flag = true;
                break;
            }
            i++;
        }
        if(!flag){
            return pID;
        }

        // Check second diagonal
        int s = BOARD_SIZE-1;
        pID = tempBoard[s][s].empty() ? -1 : tempBoard[s][s].peek().getPlayerID();
        i = BoardUtils.BOARD_SIZE-1;
        flag = false;
        while(i >= 0){
            if(tempBoard[i][i].empty() || tempBoard[i][i].peek().getPlayerID() != pID){
                flag = true;
                break;
            }
            i--;
        }
        if(!flag){
            return pID;
        }


        return -1;
    }

    /**
     * Returns the winner based on the proposed move
     */
    public static int calcWin(StackP[][] board, Player[] players, PlayerMove move){

        StackP[][] tempBoard = BoardUtils.copyBoard(board);
        Player[] newPlayers = BoardUtils.copyPlayers(players);

        BoardUtils.updateBoard(move, tempBoard, newPlayers);

        return BoardUtils.checkWinner(tempBoard);

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

    public static Player[] copyPlayers(Player[] players){
        Player[] ret = new Player[players.length];

        for(int i = 0; i < players.length; i++){
            ret[i] = players[i].copyPlayer();
        }

        return ret;
    }

}
