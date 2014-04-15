package Players.AJR2546;

import Interface.PlayerMove;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by Andres on 4/15/2014.
 */
public class ThreadedScorer implements Runnable{
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

        StackP[][] tempBoard = BoardUtils.copyBoard(board);

        BoardUtils.updateBoard(move, tempBoard, players);


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
        pID = tempBoard[BoardUtils.BOARD_SIZE-1][BoardUtils.BOARD_SIZE-1].empty() ? -1 : tempBoard[BoardUtils.BOARD_SIZE-1][BoardUtils.BOARD_SIZE-1].peek().getPlayerID();
        i = BoardUtils.BOARD_SIZE-2;
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
     * Score a non-winning move
     * @return the score
     */
    private int scorePlay(){
        int score = 0;

        StackP[][] tmpBoard = BoardUtils.copyBoard(board);

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
            score += 5; //Prefer to unload game pieces
        }

        //Score at random
        return score + randomizer.nextInt(5); //Add a random element to this
    }

    public void run(){
        pWin = calcWin();

        if(pWin != -1){
            score = scorePlay();
        }

        try {
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    public int getScore(){
        return score;
    }

    public int getpWin(){
        return pWin;
    }

}