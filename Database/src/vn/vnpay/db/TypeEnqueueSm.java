package vn.vnpay.db;

/**
 * Created by IntelliJ IDEA.
 * User: Hoang Dinh Du
 * Date: 01/07/2012
 * Time: 21:33
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("unchecked")
public class TypeEnqueueSm {
    private Integer msg_id;
    private Integer cmd_status;

    public TypeEnqueueSm() {
        this.msg_id = 0;
        this.cmd_status = 0;
    }

    public Integer getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(Integer msg_id) {
        this.msg_id = msg_id;
    }

    public Integer getCmd_status() {
        return cmd_status;
    }

    public void setCmd_status(Integer cmd_status) {
        this.cmd_status = cmd_status;
    }
}
