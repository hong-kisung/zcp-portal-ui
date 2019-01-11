package com.skcc.cloudz.zcp.portal.management.user.vo;

import java.util.List;

public class UserVo {
    private String id;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private Boolean enabled;
    private Boolean emailVerified;
    private Boolean temporary;
    private String period;
    private String type;
    private List<String> actions;
    private String clusterRole;
    private String namespace;
    private String keyword;
    private Boolean totp;
    private Boolean zdbAdmin;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public Boolean getEmailVerified() {
        return emailVerified;
    }
    
    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    
    public Boolean getTemporary() {
        return temporary;
    }
    
    public void setTemporary(Boolean temporary) {
        this.temporary = temporary;
    }
    
    public String getPeriod() {
        return period;
    }
    
    public void setPeriod(String period) {
        this.period = period;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public List<String> getActions() {
        return actions;
    }
    
    public void setActions(List<String> actions) {
        this.actions = actions;
    }
    
    public String getClusterRole() {
        return clusterRole;
    }
    
    public void setClusterRole(String clusterRole) {
        this.clusterRole = clusterRole;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public Boolean getTotp() {
        return totp;
    }
    
    public void setTotp(Boolean totp) {
        this.totp = totp;
    }
    
    public Boolean getZdbAdmin() {
        return zdbAdmin;
    }

    public void setZdbAdmin(Boolean zdbAdmin) {
        this.zdbAdmin = zdbAdmin;
    }
    
    public static class Attribute {
        private String key;
        private String value;
        
        public String getKey() {
            return key;
        }
        
        public void setKey(String key) {
            this.key = key;
        }
        
        public String getValue() {
            return value;
        }
        
        public void setValue(String value) {
            this.value = value;
        }
    }

}
