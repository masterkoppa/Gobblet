/**
 * Created by andres on 3/13/14.
 */
package Players.AJR2546;

import Engine.Logger;
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
        //dumpGameState();
    }



    @Override
    public void playerInvalidated(int i) {

    }

    @Override
    public PlayerMove move() {

        //Graph g = Graph.buildFromBoard(board, players, myID);
        //g.printGraph();

        PlayerMove[] moves = generatePossibleMoves();
        System.out.println("Possible moves: " + moves.length);

        HashMap<Integer, PlayerMove> moveScored = new HashMap<Integer, PlayerMove>();
        ArrayList<ThreadedScorer> list = new ArrayList<ThreadedScorer>();
        ArrayList<Thread> threads = new ArrayList<Thread>();
        CyclicBarrier barrier = new CyclicBarrier(moves.length + 1);

        int count = 0;
        // Pick a player move based on win condition, otherwise fall back to random
        for(PlayerMove m : moves){

            ThreadedScorer tmp = new ThreadedScorer(m, board, players, barrier, myID);
            //tmp.run();
            Thread t = new Thread(tmp);
            t.start();

            list.add(tmp);
            threads.add(t);

            count++;

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

        for(Thread t : threads){
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ThreadedScorer.counter.set(0);

        for(ThreadedScorer ts : list){
            moveScored.put(ts.getScore(), ts.getMove());
        }

        List<Integer> temp = new ArrayList<Integer>(new TreeSet<Integer>(moveScored.keySet()));

        int index = temp.size()-1;


        //System.out.println("Move Score: " + temp.get(index));


        System.out.println("Player making move: " + moveScored.get(temp.get(index)));
        System.out.println("Player Score:       " + temp.get(index));

        return moveScored.get(temp.get(index));
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
        return BoardUtils.generatePossibleMoves(board,players,this.getID());
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
