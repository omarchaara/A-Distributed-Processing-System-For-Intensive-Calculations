package masterproject;

import java.io.Serializable;

public class SlaveRequest implements Serializable {
    private final Utils.SubMatrix subMatrix;
    private final String filter;
    private final String[] args;

    public SlaveRequest(Utils.SubMatrix subMatrix, String filter, String[] args) {
        this.subMatrix = subMatrix;
        this.filter = filter;
        this.args = args;
    }

    public Utils.SubMatrix getSubMatrix() {
        return subMatrix;
    }

    public String getFilter() {
        return filter;
    }

    public String[] getArgs() {
        return args;
    }
}
