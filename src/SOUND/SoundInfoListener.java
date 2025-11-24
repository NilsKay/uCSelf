package SOUND;
import java.util.Vector;
/**
 * This interface declares Methods used to handle Sound Events
 */
public interface SoundInfoListener {
/**
 * request to update the display
 * @param code is the message code from the server (see Gravd_Commands for the meaning of these codes)
 * @param res the Vector of received parameters, line by line 
 * @param broad if true, this is a broadcast !
 */
public void displayRT(RandomTable rt);
    
} // end of the interface
