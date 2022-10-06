package it.smartcommunitylab.aac.core.provider;

public interface LoginProvider {
    
    public String getAuthority();

    public String getProvider();

    public String getRealm();
    
    /*
     * Config
     */
    public String getName();

    public String getDescription();

    public Integer getPosition();

    /*
     * Login
     */
    public String getLoginUrl();

    /*
     * Template render
     */
    public String getTemplate();

    public String getIconUrl();

    public String getCssClass();
}