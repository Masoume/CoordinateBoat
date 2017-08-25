/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sami.mission;

/**
 *
 * @author pscerri
 */
public class RequirementSpecification implements java.io.Serializable {

    String requirementText;
    String name = null;
    boolean filled = false;
    Object filledBy = false;

    public RequirementSpecification(String name) {
        this.name = name;
        System.out.println("Created RequirementSpecification " + name);
    }

    public boolean isFilled() {
        return filled;
    }

    public void setFilled(boolean filled) {
        this.filled = filled;
        System.out.println("Filled RequirementSpecification " + name);
    }

    public Object getFilledBy() {
        return filledBy;
    }

    public void setFilledBy(Object filledBy) {
        this.filledBy = filledBy;
        System.out.println("Filled RequirementSpecification " + name + " with " + filledBy);
    }

    public String getRequirementText() {
        return requirementText;
    }

    public void setRequirementText(String requirementText) {
        this.requirementText = requirementText;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
