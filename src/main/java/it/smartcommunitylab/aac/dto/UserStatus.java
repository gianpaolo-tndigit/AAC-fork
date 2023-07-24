package it.smartcommunitylab.aac.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import it.smartcommunitylab.aac.model.SubjectStatus;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Valid
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserStatus {

    @NotNull
    private SubjectStatus status;

    public SubjectStatus getStatus() {
        return status;
    }

    public void setStatus(SubjectStatus status) {
        this.status = status;
    }
}
