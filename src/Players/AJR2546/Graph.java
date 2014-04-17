package Players.AJR2546;

import Interface.PlayerMove;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Andres on 4/15/2014.
 */
public class Graph {

    private ArrayList<Node> nodes;
    private Node initialNode;

    private Graph(){
        nodes = new ArrayList<Node>();
    }

    public static Graph buildFromBoard(StackP[][] board, Player[] players, int myID){
        Graph graph = new Graph();

        Node node = new Node(board, players, myID); //Build the initial node
        graph.nodes.add(node);
        graph.initialNode = node;

        node.buildNeighbors();

        List<Node> newNodes = Node.buildNextMove(node, graph.nodes);
        for(Node n : newNodes){
            if(!graph.nodes.contains(n)){
                graph.nodes.add(n);
            }
        }

        int counter = 0;
        while(newNodes.size() < 300 && counter < 5) {
            newNodes = generateFromChildren(newNodes, graph);
            counter++;
            System.out.println(counter);
        }

        return graph;
    }

    // Change to use BFS to find the next move towards goal
    public PlayerMove getNextMove(Node goal){
        ArrayList<PlayerMove> moves = new ArrayList<PlayerMove>();
        Stack<Node> list = new Stack<Node>();
        list.push(initialNode);
        while(!list.empty()){
            Node tmp = list.pop();
            HashMap<PlayerMove, Node.Board> neighs = tmp.getNeighborsFull();

            for(PlayerMove move : neighs.keySet()){
                if(neighs.get(move).equals(goal)){
                    //moves.add()
                }
            }
        }
        return moves.get(0);
    }

    public void printGraph(){

        System.out.println("Nodes: " + nodes.size());
    }



    public static void main(String[] args){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
        String formattedDate = sdf.format(date);
        System.out.println(formattedDate); // 12/01/2011 4:48:16 PM
        StackP[][] board = new StackP[BoardUtils.BOARD_SIZE][BoardUtils.BOARD_SIZE];
        Player[] players = new Player[BoardUtils.NUM_PLAYERS];

        for(int i = 0; i < players.length; i++){
            players[i] = new Player(i+1);
        }

        for(int r = 0; r < board.length; r++){
            for(int c = 0; c < board.length; c++){
                board[r][c] = new StackP();
            }
        }

        Graph g = new Graph();
        Node n = new Node(board, players, 1); //Build the initial node
        g.nodes.add(n);

        n.buildNeighbors();
        n.printNode();


        List<Node> newNodes = Node.buildNextMove(n, g.nodes);
        for(Node nN : newNodes){

            if(!g.nodes.contains(nN)){
                g.nodes.add(nN);
            }
        }
        //g.nodes.addAll(newNodes);
        int counter = 0;
        while(newNodes.size() < 300 && counter < 5) {
            newNodes = generateFromChildren(newNodes, g);
            counter++;
            System.out.println(counter);
        }

        System.out.println("Moves built: " + newNodes.size());
        date = new Date();
        String formattedDate1 = sdf.format(date);
        System.out.println(formattedDate1); // 12/01/2011 4:48:16 PM

    }

    private static List<Node> generateFromChildren(List<Node> nodes, Graph g){
        ArrayList<Node> treeLayer = new ArrayList<Node>();
        Random r = new Random();

        for(Node n : nodes) {
            List<Node> tmp = Node.buildNextMove(n, g.nodes);
            for(Node t : tmp){
                if(!g.nodes.contains(t)){
                    g.nodes.add(t);
                    treeLayer.add(t);
                    //t.printSolo();
                }
            }
        }
        System.out.println("Total in Layer: " + treeLayer.size());
        return treeLayer;
    }

}


