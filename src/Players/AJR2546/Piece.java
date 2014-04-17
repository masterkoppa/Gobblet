package Players.AJR2546;

/**
 * Created by Andres on 4/15/2014.
 */
public class Piece{

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

    public boolean equals(Object o){
        try{
            Piece p = (Piece)o;

            if(p.size != this.size || p.playerID != this.playerID){
                return false;
            }else {
                return true;
            }
        }catch (ClassCastException ex){
            return false;
        }
    }
}
