package sami.handler;

import sami.event.OutputEvent;
import sami.mission.Token;
import java.util.ArrayList;

/**
 *
 * @author pscerri
 */
public interface EventHandlerInt {

    public void invoke(OutputEvent e, ArrayList<Token> tokens);

//    public void terminate(OutputEvent e, ArrayList<Token> tokens);
}
