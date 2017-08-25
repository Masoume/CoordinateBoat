/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sami.mission;

import sami.event.Event;
import java.util.ArrayList;

/**
 *
 * @author pscerri
 */
public class TestCase {

    ArrayList<Event> events;

    public TestCase(ArrayList<Event> events) {
        this.events = events;
    }
        
 
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Event event : events) {
            sb.append(event + " ");
        }
        return sb.toString();
    }
}
