package fr.insee.rmes.modules.users.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String stampClaim;
    private String roleClaim;
    private String idClaim;
    private String sourceClaim;
    private String inseeGroupClaim;
    private String hieApplicationPrefix;
    private RoleClaim roleClaimConfig = new RoleClaim();

    public String getStampClaim() {
        return stampClaim;
    }

    public void setStampClaim(String stampClaim) {
        this.stampClaim = stampClaim;
    }

    public String getRoleClaim() {
        return roleClaim;
    }

    public void setRoleClaim(String roleClaim) {
        this.roleClaim = roleClaim;
    }

    public String getIdClaim() {
        return idClaim;
    }

    public void setIdClaim(String idClaim) {
        this.idClaim = idClaim;
    }

    public String getSourceClaim() {
        return sourceClaim;
    }

    public void setSourceClaim(String sourceClaim) {
        this.sourceClaim = sourceClaim;
    }

    public RoleClaim getRoleClaimConfig() {
        return roleClaimConfig;
    }

    public void setRoleClaimConfig(RoleClaim roleClaimConfig) {
        this.roleClaimConfig = roleClaimConfig;
    }

    public String getHieApplicationPrefix() {
        return hieApplicationPrefix;
    }

    public void setHieApplicationPrefix(String hieApplicationPrefix) {
        this.hieApplicationPrefix = hieApplicationPrefix;
    }

    public String getInseeGroupClaim() {
        return inseeGroupClaim;
    }

    public void setInseeGroupClaim(String inseeGroupClaim) {
        this.inseeGroupClaim = inseeGroupClaim;
    }

    public static class RoleClaim {
        private String roles;

        public String getRoles() {
            return roles;
        }

        public void setRoles(String roles) {
            this.roles = roles;
        }
    }
}