package Players.AJR2546;

import Interface.PlayerMove;

import java.util.*;

/**
 * Created by Andres on 4/15/2014.
 */
public class Node {

    public class Board{
        StackP[][] board;
        Player[] players;

        public Board(StackP[][] board, Player[] players){
            this.board = board;
            this.players = players;
        }

        public String toString(){
            String ret = "";
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

                ret += line + "\n";
            }
            return ret;
        }

        public void dumpGameState() {
            System.out.println(this);
        }

        public int getWinner(){
            return BoardUtils.checkWinner(board);
        }

    }

    private Board board;
    private HashMap<PlayerMove, Board> neighbors;
    private int playerTurn;

    public Node(StackP[][] board, Player[] players, int playerTurn){
        this.board = new Board(board, players);
        this.neighbors = new HashMap<PlayerMove, Board>();
        this.playerTurn = playerTurn;
    }

    public void printSolo(){
        System.out.println("===================");
        System.out.println("   Current board");
        System.out.println("===================");
        this.board.dumpGameState();
    }

    public void printNode(){
        printSolo();
        for(PlayerMove move : neighbors.keySet()){
            System.out.println("Move " + move);
            neighbors.get(move).dumpGameState();
        }
    }

    public Collection<Board> getNeighbors(){
        return neighbors.values();
    }

    public HashMap<PlayerMove, Board> getNeighborsFull(){
        return this.neighbors;
    }

    public boolean isSame(Board board, int playerTurn){
        return this.isSame(board.board, board.players, playerTurn);
    }

    public boolean isSame(StackP[][] board, Player[] players, int playerTurn){
        if(playerTurn != this.playerTurn){
            return false;
        }
        //System.out.println("Same player");
        for(int r = 0; r < board.length; r++){
            for(int c = 0; c < board.length; c++){
                if(!this.board.board[r][c].equals(board[r][c])){
                    //System.out.println("True");
                    return false;
                }
            }
        }
        //System.out.println("Same board");
        for(int i = 0; i < players.length; i++){
            //Test to see if they are similar enough
            ArrayList<Integer> stack1 = new ArrayList<Integer>();
            ArrayList<Integer> stack2 = new ArrayList<Integer>();

            for(int s = 0; s < 3; s++){
                stack1.add(this.board.players[i].getStackSize(s));
                stack2.add(players[i].getStackSize(s));
            }

            Collections.sort(stack1);
            Collections.sort(stack2);

            for(int s = 0; s < 3; s++){
                //System.out.println(stack1.get(s) + " == " + stack2.get(s));
                if(!stack1.get(s).equals(stack2.get(s))){
                    return false;
                }
            }
        }

        return true;
    }

    public boolean equals(Object o){
        //System.out.println("Called eq");
        try{
            Node n = (Node)o;

            Board b = n.board;
            int pTurn = n.playerTurn;

            return isSame(b,pTurn);

        }catch (ClassCastException ex){
            return false;
        }
    }

    public void buildNeighbors(){

        if(this.board.getWinner() != -1){
            return;
        }

        PlayerMove[] moves = BoardUtils.generatePossibleMoves(this.board.board, this.board.players, playerTurn);
        for(PlayerMove m : moves){

            //Make copies
            StackP[][] board = BoardUtils.copyBoard(this.board.board);
            Player[] players = BoardUtils.copyPlayers(this.board.players);

            BoardUtils.updateBoard(m,board,players);

            Board newBoard = new Board(board, players);

            this.neighbors.put(m, newBoard);
        }
    }

    public static List<Node> buildNextMove(Node currentNode, List<Node> currNodes){
        int newTurn = (currentNode.playerTurn % 2) + 1;
        ArrayList<Node> newNodes = new ArrayList<Node>();

        for(Board b : currentNode.getNeighbors()){
            newNodes.add(new Node(b.board, b.players, newTurn));
        }

        for(Node n : newNodes){
            n.buildNeighbors();
        }

        //System.out.println("Moves generated: " + newNodes.size());

        return newNodes;
    }


}
