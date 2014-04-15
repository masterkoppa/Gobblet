package Players.AJR2546;

/**
 * Created by Andres on 4/15/2014.
 */
public class Player{

    private int myID;
    private StackP[] stacks;

    public Player(int id){
        stacks = new StackP[BoardUtils.NUM_STACKS];

        for(int i = 0; i < BoardUtils.NUM_STACKS; i++){
            stacks[i] = new StackP();
            stacks[i].push(new Piece(id, 1));
            stacks[i].push(new Piece(id, 2));
            stacks[i].push(new Piece(id, 3));
            stacks[i].push(new Piece(id, 4));
        }
        this.myID = id;
    }

    public int getMyID(){
        return myID;
    }

    public String getStackString(){
        String ret = "";

        for(int i = 0; i < BoardUtils.NUM_STACKS; i++){
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
        Piece[] ret = new Piece[BoardUtils.NUM_STACKS];
        for(int i = 0; i < BoardUtils.NUM_STACKS; i++){
            if(!stacks[i].empty())
                ret[i] = stacks[i].peek();
        }
        return ret;
    }

    public Piece takeFromStack(int stack){
        return stacks[stack].pop();
    }

    public Player copyPlayer(){
        Player p = new Player(myID);

        StackP[] stacks = new StackP[this.stacks.length];

        for(int i = 0; i < stacks.length; i++){
            stacks[i] = this.stacks[i].copy();
        }

        p.stacks = stacks;

        return p;
    }

}