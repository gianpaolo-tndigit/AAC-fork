package it.smartcommunitylab.aac.password.templates;

import it.smartcommunitylab.aac.SystemKeys;
import it.smartcommunitylab.aac.templates.model.FixedTemplateModel;
import java.util.Arrays;

public class PasswordResetSuccessTemplate extends FixedTemplateModel {

    public static final String TEMPLATE = "resetpwd_success";
    private static final String[] KEYS = { "resetpwd_success.text" };

    public PasswordResetSuccessTemplate(String realm) {
        super(SystemKeys.AUTHORITY_PASSWORD, realm, null, TEMPLATE, Arrays.asList(KEYS));
    }
}
