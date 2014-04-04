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

public class AJR2546 implements PlayerModule, GobbletPart1 {


    private class Piece{

        private int size;
        private int playerID;

        public Piece(int playerID, int size){
            this.size = size;
            this.playerID = playerID;
        }

        public int getSize(){
            return size;
        }

        public int getPlayerID(){
            return playerID;
        }

        public String toString(){
            return size + "(" + playerID + ") ";
        }
    }

    private class StackP extends Stack<Piece> {
        // No additional code needed

        public StackP copy(){
            StackP ret = new StackP();

            for(Piece i : this){
                ret.push(i);
            }

            return ret;
        }
    }

    private class Player{

        private int myID;
        private StackP[] stacks;

        public Player(int id){
            stacks = new StackP[NUM_STACKS];

            for(int i = 0; i < NUM_STACKS; i++){
                stacks[i] = new StackP();
                stacks[i].push(new Piece(id, 1));
                stacks[i].push(new Piece(id, 2));
                stacks[i].push(new Piece(id, 3));
                stacks[i].push(new Piece(id, 4));
            }
            this.myID = id;
        }

        public String getStackString(){
            String ret = "";

            for(int i = 0; i < NUM_STACKS; i++){
                if(!stacks[i].empty())
                    ret += " " + stacks[i].size() + " ";
                else
                    ret += " _ ";
            }

            return ret;
        }

        public int getStackSize(int stack){
            return !stacks[stack].empty() ? stacks[stack].size() : -1;
        }

        /*
         * Returns the list of all the top stacks, null if empty
         */
        public Piece[] peekAtStacks(){
            Piece[] ret = new Piece[NUM_STACKS];
            for(int i = 0; i < NUM_STACKS; i++){
                if(!stacks[i].empty())
                    ret[i] = stacks[i].peek();
            }
            return ret;
        }

        public Piece takeFromStack(int stack){
            return stacks[stack].pop();
        }

    }


    // Constants
    private static final int BOARD_SIZE = 4;
    private static final int NUM_STACKS = 3;
    private static final int NUM_PLAYERS = 2;


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
        for(int r = 0; r < BOARD_SIZE; r++){
            String line = "";
            for(int c = 0; c < BOARD_SIZE; c++){

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

    private static StackP[][] copyBoard(StackP[][] board){
        StackP[][] ret = new StackP[board.length][board[0].length];
        for(int row = 0; row < board.length; row++){
            for(int col = 0; col < board[row].length; col++){
                ret[row][col] = board[row][col].copy();
            }
        }
        return ret;
    }

    @Override
    public void init(Logger logger, int pID) {
        this.log = logger;
        this.myID = pID;
        this.randomizer = new Random();
        //this.randomizer = new Random(10000L);




        // Init the board and players
        players = new Player[NUM_PLAYERS];

        for(int i = 0; i < NUM_PLAYERS; i++){
            players[i] = new Player(i+1);// Start pID as 1
        }

        board = new StackP[BOARD_SIZE][BOARD_SIZE];

        for(int r = 0; r < BOARD_SIZE; r++){
            for(int c = 0; c < BOARD_SIZE; c++){
                board[r][c] = new StackP();
            }
        }
    }

    @Override
    public void lastMove(PlayerMove playerMove) {
        updateBoard(playerMove, board);

        // Update the players
        if(playerMove.getStartRow() == -1){
            int pID = playerMove.getPlayerId();
            int stack = playerMove.getStack();

            Piece p = players[pID-1].takeFromStack(stack-1);
        }
        dumpGameState();
    }

    private void updateBoard(PlayerMove playerMove, StackP[][] board){
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

    @Override
    public void playerInvalidated(int i) {

    }

    @Override
    public PlayerMove move() {
        PlayerMove[] moves = generatePossibleMoves();
        System.out.println("Possible moves: " + moves.length);

        HashMap<Integer, PlayerMove> moveScored = new HashMap<Integer, PlayerMove>();

        // Pick a player move based on win condition, otherwise fall back to random
        for(PlayerMove m : moves){


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
            }
        }

        List<Integer> temp = new ArrayList<Integer>(new TreeSet<Integer>(moveScored.keySet()));

        System.out.println("Move Score: " + temp.get(temp.size()-1));

        return moveScored.get(temp.get(temp.size()-1));
        // Pick at random
        //return moves[randomizer.nextInt(moves.length)];
    }

    /**
     * Score a non-winning move
     * @param move PlayerMove to analyze
     * @return the score
     */
    private int scorePlay(PlayerMove move){

        int score = 0; //Start with 0
        StackP[][] tmpBoard = copyBoard(board);

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
            updateBoard(move, tmpBoard);

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

        //Score at random
        return score + randomizer.nextInt(5); //Add a random element to this
    }

    /**
     * Returns the winner based on the proposed move
     */
    private int calcWin(PlayerMove move, StackP[][] board){

        StackP[][] tempBoard = copyBoard(board);

        updateBoard(move, tempBoard);


        //Check horizontal lines
        for(int row = 0; row < BOARD_SIZE; row++){
            int pID = tempBoard[row][0].empty() ? -1 : tempBoard[row][0].peek().getPlayerID();
            boolean flag = false;
            for(int col = 1; col < BOARD_SIZE; col++){
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
        for(int col = 0; col < BOARD_SIZE; col++){
            int pID = tempBoard[0][col].empty() ? -1 : tempBoard[0][col].peek().getPlayerID();
            boolean flag = false;
            for(int row = 1; row < BOARD_SIZE; row++){
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
        while(i < BOARD_SIZE){
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
        pID = tempBoard[BOARD_SIZE-1][BOARD_SIZE-1].empty() ? -1 : tempBoard[BOARD_SIZE-1][BOARD_SIZE-1].peek().getPlayerID();
        i = BOARD_SIZE-2;
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
     * Returns whether this move is a winning move for specified player or not
     * @param move - The player move to evaluate
     * @param pID - Player ID 1 or 2
     * @return
     */
    private boolean makesWin(PlayerMove move, int pID){

        // Check for lines
        int row = move.getEndRow();
        int col = move.getEndCol();

        boolean hLine = true;
        boolean vLine = true;
        boolean vert1Line = true;
        boolean vert2Line = true;

        for(int c = 0; c < BOARD_SIZE; c++){
            if(c == col && move.getStartRow() != row){
                continue;
            }
            if(getTopOwnerOnBoard(row, c) != this.getID()){
                hLine = false;
                break;
            }
        }

        for(int r = 0; r < BOARD_SIZE; r++){
            if(r == row && move.getStartCol() != col){
                continue;
            }
            if(getTopOwnerOnBoard(r, col) != this.getID()){
                vLine = false;
                break;
            }
        }

        for(int d = 0; d < BOARD_SIZE; d++){
            if(d == row && d == col && move.getStartRow() != row && move.getStartCol() != col){
                continue;
            }
            if(getTopOwnerOnBoard(d, d) != this.getID()){
                vert1Line = false;
                break;
            }
        }

        for(int d = 0; d < BOARD_SIZE; d++){
            if(BOARD_SIZE-d-1 == row && d == col && move.getStartRow() != row && move.getStartCol() != col){
                continue;
            }
            if(getTopOwnerOnBoard(BOARD_SIZE-d-1,  d) != this.getID()){
                vert2Line = false;
                break;
            }
        }

        return hLine || vLine || vert1Line || vert2Line;
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
        for(int r = 0; r < BOARD_SIZE; r++){
            for(int c = 0; c < BOARD_SIZE; c++){
                if(!board[r][c].empty() && board[r][c].peek().getPlayerID() == this.getID()){
                    available.put(new Coordinate(r,c), board[r][c].peek());
                }
            }
        }

        ArrayList<PlayerMove> intraBoardMoves = new ArrayList<PlayerMove>();

        // Generate the moves from all the available ones, valid and invalid
        for(int r = 0; r < BOARD_SIZE; r++){
            for(int c = 0; c < BOARD_SIZE; c++){

                for(Coordinate coord : available.keySet()){
                    // Dont move to the same place
                    if(coord.getRow() == r && coord.getCol() == c){
                        continue;
                    }else{
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
        for(int r = 0; r < BOARD_SIZE; r++){
            for(int c = 0; c < BOARD_SIZE; c++){
                for(int s = 0; s < NUM_STACKS; s++){

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
}
