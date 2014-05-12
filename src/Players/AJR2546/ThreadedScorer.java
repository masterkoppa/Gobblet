package Players.AJR2546;

import Interface.PlayerMove;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Andres on 4/15/2014.
 */
public class ThreadedScorer implements Runnable{

    public static final AtomicInteger counter = new AtomicInteger(1);

    private int score = 0;
    private int pWin = -1;
    private int myID = -1;

    private StackP[][] board;
    private Player[] players;
    private PlayerMove move;
    private CyclicBarrier barrier;
    private Random randomizer;

    public ThreadedScorer(PlayerMove move, StackP[][] board, Player[] players, CyclicBarrier barrier, int myID){
        this.board = board;
        this.players = players;
        this.move = move;
        this.barrier = barrier;
        this.myID = myID;
        this.randomizer = new Random();
    }

    public PlayerMove getMove(){
        return move;
    }

    /**
     * Returns the winner based on the proposed move
     */
    private int calcWin(){
        return BoardUtils.calcWin(board, players, move);
    }

    /**
     * Score a non-winning move
     * @return the score
     */
    private int scorePlay(){
        int score = 0;

        StackP[][] tmpBoard = BoardUtils.copyBoard(board);
        Player[] players = BoardUtils.copyPlayers(this.players);

        int sRow = move.getStartRow();
        int sCol = move.getStartCol();

        int eRow = move.getEndRow();
        int eCol = move.getEndCol();

        // Check if move is an enemy piece
        if(!tmpBoard[eRow][eCol].empty()){

            //Is it my piece?
            if(tmpBoard[eRow][eCol].peek().getPlayerID() == myID){
                score -= tmpBoard[eRow][eCol].peek().getSize(); //Remove the size that I just hid
            }else{
                score += (move.getSize() - tmpBoard[eRow][eCol].peek().getSize()) + 3; //Add the size that I removed from my opponent
            }
            BoardUtils.updateBoard(move, tmpBoard, players);

            //Was I already covering one?
            if(sRow == -1 || tmpBoard[sRow][sCol].empty()){
                score += 1; //Add 1 if we leave an empty space behind or from hand
            }else if(tmpBoard[sRow][sCol].peek().getPlayerID() == myID){
                score = score * 2;//Double the score since we uncovered our own
            }else{
                score = score / 2;
            }

        }

        // Do next possible move analysis

        // Generate all the possible moves for the opponent

        // If any of these opponent moves generates a winning board for opponent, score = - score

        // If any of these opponent moves generates for player, score = abs(score) * 3


        // At the end add any preferential moves like take corners or unload pieces first

        if(move.getStartRow() == -1){
            score += 10; //Prefer to unload game pieces
        }


        // Check to see if my move makes them win
        int enemyID = ((myID) % 2) + 1;

        PlayerMove[] enemyMoves = BoardUtils.generatePossibleMoves(tmpBoard,players, enemyID);

        for(PlayerMove m: enemyMoves){
            int pWin = BoardUtils.calcWin(tmpBoard, players, m);

            if(pWin == enemyID){
                return Integer.MIN_VALUE;
            }else if(pWin == myID){
                score += 10;
            }
        }


        //Score at random
        return score + randomizer.nextInt(5); //Add a random element to this
    }

    public void run(){
        pWin = calcWin();

        if(pWin == myID){
            score = Integer.MAX_VALUE;
        }else if(pWin != -1){
            score = Integer.MIN_VALUE;
        }else{
            score = scorePlay();
        }
        /*
        try {
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }*/
        //System.out.println("Counter: " + counter.addAndGet(1));

    }

    public int getScore(){
        //System.out.println("Score: " + score);
        return score;
    }

    public int getpWin(){
        return pWin;
    }

}