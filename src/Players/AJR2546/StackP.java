package Players.AJR2546;

import java.util.Stack;

/**
 * Created by Andres on 4/15/2014.
 */
public class StackP extends Stack<Piece> {
    // No additional code needed

    public StackP copy(){
        StackP ret = new StackP();

        for(Piece i : this){
            ret.push(i);
        }

        return ret;
    }

    public boolean equals(Object o){
        try{
            StackP p = (StackP)o;

            if(p.size() == this.size()){
                for(int i = 0; i < this.size(); i++){
                    if(!p.get(i).equals(this.get(i))){
                        return false;
                    }
                }
            }else{
                return false;
            }
            return true;

        }catch (ClassCastException ex){
            return false;
        }
    }
}
