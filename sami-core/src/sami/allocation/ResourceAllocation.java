package sami.allocation;

import com.perc.mitpas.adi.common.datamodels.AbstractAsset;
import com.perc.mitpas.adi.mission.planning.task.ITask;
import java.util.Map;

/**
 *
 * @author nbb
 */
public class ResourceAllocation {

    Map<ITask, AbstractAsset> allocation;
    Map<ITask, Long> taskTimings;

    public ResourceAllocation() {
    }

    public ResourceAllocation(Map<ITask, AbstractAsset> allocation, Map<ITask, Long> taskTimings) {
        this.allocation = allocation;
        this.taskTimings = taskTimings;
    }

    public Map<ITask, AbstractAsset> getAllocation() {
        return allocation;
    }

    public void setAllocation(Map<ITask, AbstractAsset> allocation) {
        this.allocation = allocation;
    }

    public Map<ITask, Long> getTaskTimings() {
        return taskTimings;
    }

    public void setTaskTimings(Map<ITask, Long> taskTimings) {
        this.taskTimings = taskTimings;
    }

    @Override
    public ResourceAllocation clone() {
        ResourceAllocation clone = new ResourceAllocation();
        for (ITask task : allocation.keySet()) {
            clone.allocation.put(task, allocation.get(task));
        }
        for (ITask task : taskTimings.keySet()) {
            clone.taskTimings.put(task, taskTimings.get(task));
        }
        return clone;
    }

    public String toString() {
        String ret = "";
        for (ITask task : allocation.keySet()) {
            AbstractAsset value = allocation.get(task);
            ret += "\n\t" + task.getName() + " (" + task.getDescription() + ") -> " + value.getName() + " (" + value.getType() + ");";
        }
        return ret;
    }
}
