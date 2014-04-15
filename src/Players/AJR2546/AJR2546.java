/**
 * Created by andres on 3/13/14.
 */
package Players.AJR2546;

import Engine.Logger;
import Interface.Coordinate;
import Interface.GobbletPart1;
import Interface.PlayerModule;
import Interface.PlayerMove;

import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class AJR2546 implements PlayerModule, GobbletPart1 {



    // Instance variables
    private Logger log;
    private StackP[][] board;
    private Player[] players;
    private int myID;
    private Random randomizer;

    @Override
    public int getID() {
        return myID;
    }

    @Override
    public int getTopSizeOnBoard(int row, int col) {
        return !board[row][col].empty() ? board[row][col].peek().getSize() : -1;
    }

    @Override
    public int getTopOwnerOnBoard(int row, int col) {
        return !board[row][col].empty() ? board[row][col].peek().getPlayerID() : -1;
    }

    @Override
    public int getTopSizeOnStack(int pID, int stack) {
        return players[pID - 1].getStackSize(stack - 1);
    }

    @Override
    public void dumpGameState() {
        for(int r = 0; r < BoardUtils.BOARD_SIZE; r++){
            String line = "";
            for(int c = 0; c < BoardUtils.BOARD_SIZE; c++){

                StackP cell = board[r][c];

                if(cell.empty()){
                    line += " []  ";
                }else{
                    line += cell.peek();
                }

            }

            // Print the first player
            if(r == 0){
                line += players[0].getStackString();
            }else if(r == 2){
                line += players[1].getStackString();
            }

            System.out.println(line);
        }
    }



    @Override
    public void init(Logger logger, int pID) {
        this.log = logger;
        this.myID = pID;
        this.randomizer = new Random();
        //this.randomizer = new Random(10000L);




        // Init the board and players
        players = new Player[BoardUtils.NUM_PLAYERS];

        for(int i = 0; i < BoardUtils.NUM_PLAYERS; i++){
            players[i] = new Player(i+1);// Start pID as 1
        }

        board = new StackP[BoardUtils.BOARD_SIZE][BoardUtils.BOARD_SIZE];

        for(int r = 0; r < BoardUtils.BOARD_SIZE; r++){
            for(int c = 0; c < BoardUtils.BOARD_SIZE; c++){
                board[r][c] = new StackP();
            }
        }
    }

    @Override
    public void lastMove(PlayerMove playerMove) {
        BoardUtils.updateBoard(playerMove, board, players);

        // Update the players
        if(playerMove.getStartRow() == -1){
            int pID = playerMove.getPlayerId();
            int stack = playerMove.getStack();

            players[pID-1].takeFromStack(stack-1);
        }
        dumpGameState();
    }



    @Override
    public void playerInvalidated(int i) {

    }

    @Override
    public PlayerMove move() {
        PlayerMove[] moves = generatePossibleMoves();
        System.out.println("Possible moves: " + moves.length);

        HashMap<Integer, PlayerMove> moveScored = new HashMap<Integer, PlayerMove>();
        ArrayList<ThreadedScorer> list = new ArrayList<ThreadedScorer>();
        CyclicBarrier barrier = new CyclicBarrier(moves.length + 1);
        // Pick a player move based on win condition, otherwise fall back to random
        for(PlayerMove m : moves){

            ThreadedScorer tmp = new ThreadedScorer(m, board, players, barrier, myID);
            //tmp.run();
            Thread t = new Thread(tmp);
            t.start();

            list.add(tmp);

            /*
            //Analyze for win
            int winPID = calcWin(m, this.board);

            if(winPID == myID){
                return m;
            }else if(winPID != -1){
                // Avoid this move
                System.out.println("Enemy wins");
                System.out.println("Enemy wins");
                System.out.println("Enemy wins");
                System.out.println("Enemy wins");
                System.out.println("Enemy wins");
                System.out.println("Enemy wins");
            }else{
                moveScored.put(scorePlay(m), m);
            }*/
        }

        try {
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }

        for(ThreadedScorer ts : list){
            if(ts.getpWin() == myID){
                return ts.getMove();
            }else if(ts.getpWin() != -1){
                System.out.println("Enemy wins");
                System.out.println("Enemy wins");
                System.out.println("Enemy wins");
                System.out.println("Enemy wins");
                System.out.println("Enemy wins");
                System.out.println("Enemy wins");
            }else{
                moveScored.put(ts.getScore(), ts.getMove());
            }
        }

        List<Integer> temp = new ArrayList<Integer>(new TreeSet<Integer>(moveScored.keySet()));

        System.out.println("Move Score: " + temp.get(temp.size()-1));

        return moveScored.get(temp.get(temp.size()-1));
        // Pick at random
        //return moves[randomizer.nextInt(moves.length)];
    }

    /**
     * Generates the list of all valid and possible moves in the board.
     *
     * From here we will choose what moves to use.
     * @return PlayerMove array of what's possible
     */
    private PlayerMove[] generatePossibleMoves(){

        // Get all the pieces that I can move
        HashMap<Coordinate, Piece> available = new HashMap<Coordinate, Piece>();


        // Find all the pieces at the top of the board
        for(int r = 0; r < BoardUtils.BOARD_SIZE; r++){
            for(int c = 0; c < BoardUtils.BOARD_SIZE; c++){
                if(!board[r][c].empty() && board[r][c].peek().getPlayerID() == this.getID()){
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
                        intraBoardMoves.add(new PlayerMove(this.getID(), 0, p.getSize(), coord, new Coordinate(r,c)));
                    }
                }

            }
        }

        // Store the valid moves for intra board things
        ArrayList<PlayerMove> validMoves = new ArrayList<PlayerMove>();

        // Validate all the moves generated
        for(PlayerMove i : intraBoardMoves){
            if(validateMove(i)){
                validMoves.add(i);
            }
        }

        ArrayList<PlayerMove> stackMoves = new ArrayList<PlayerMove>();
        // Deal with the player stacks
        for(int r = 0; r < BoardUtils.BOARD_SIZE; r++){
            for(int c = 0; c < BoardUtils.BOARD_SIZE; c++){
                for(int s = 0; s < BoardUtils.NUM_STACKS; s++){

                    Piece p = players[this.getID()-1].peekAtStacks()[s];

                    // If the stack is empty, continue
                    if(p == null){
                        continue;
                    }
                    stackMoves.add(new PlayerMove(this.getID(), s+1, p.getSize(),
                            new Coordinate(-1,-1), new Coordinate(r,c)));
                }
            }
        }

        // Validate all these moves
        for(PlayerMove i : stackMoves){
            if(validateMove(i))
                validMoves.add(i);
        }

        PlayerMove[] ret = new PlayerMove[validMoves.size()];

        validMoves.toArray(ret);

        return ret;
    }

    /**
     * Validates the move based on the goblet rules
     *
     * @param move - The move to be validated
     * @return True if valid, False otherwise
     */
    private boolean validateMove(PlayerMove move){
        return BoardUtils.validateMove(move, board, players);
    }
}
