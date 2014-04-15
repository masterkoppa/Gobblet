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
}
