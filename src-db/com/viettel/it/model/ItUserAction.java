

import com.viettel.model.Action;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by hanh on 5/15/2017.
 */
@Entity
@Table(name = "IT_USER_ACTION")
public class ItUserAction implements Serializable {

    private ItUserActionId id;
    private Action actionUser;
    private ItUsers user;

    public ItUserAction() {
    }

    public ItUserAction(ItUserActionId id) {
        this.id = id;
    }

    @EmbeddedId
    @AttributeOverrides({ @AttributeOverride(name = "actionId", column = @Column(name = "ACTION_ID", nullable = false, precision = 22, scale = 0)),
            @AttributeOverride(name = "userId", column = @Column(name = "USER_ID", nullable = false, precision = 22, scale = 0)) })
    public ItUserActionId getId() {
        return id;
    }

    public void setId(ItUserActionId id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ACTION_ID", nullable = false, insertable = false, updatable = false)
    public Action getActionUser() {
        return actionUser;
    }

    public void setActionUser(Action actionUser) {
        this.actionUser = actionUser;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ID", nullable = false, insertable = false, updatable = false)
    public ItUsers getUser() {
        return user;
    }

    public void setUser(ItUsers user) {
        this.user = user;
    }
}
