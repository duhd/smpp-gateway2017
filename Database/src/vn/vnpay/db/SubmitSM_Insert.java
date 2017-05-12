package vn.vnpay.db;

/**
 * Created by hoangdinhdu@gmail.com on 11/17/2014.
 */
public class SubmitSM_Insert {
    private Integer message_id;
    private Integer command_status;

    public SubmitSM_Insert(Integer message_id,Integer command_status){
        setMessage_id(message_id);
        setCommand_status(command_status);
    }

    public Integer getMessage_id() {
        return message_id;
    }

    public void setMessage_id(Integer message_id) {
        this.message_id = message_id;
    }

    public Integer getCommand_status() {
        return command_status;
    }

    public void setCommand_status(Integer command_status) {
        this.command_status = command_status;
    }
}
