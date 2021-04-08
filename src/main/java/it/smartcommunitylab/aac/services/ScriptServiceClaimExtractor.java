package it.smartcommunitylab.aac.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.smartcommunitylab.aac.claims.Claim;
import it.smartcommunitylab.aac.claims.ClaimsSet;
import it.smartcommunitylab.aac.claims.DefaultClaimsSet;
import it.smartcommunitylab.aac.claims.ResourceClaimsExtractor;
import it.smartcommunitylab.aac.claims.ScriptExecutionService;
import it.smartcommunitylab.aac.claims.model.AbstractClaim;
import it.smartcommunitylab.aac.claims.model.BooleanClaim;
import it.smartcommunitylab.aac.claims.model.DateClaim;
import it.smartcommunitylab.aac.claims.model.NumberClaim;
import it.smartcommunitylab.aac.claims.model.SerializableClaim;
import it.smartcommunitylab.aac.claims.model.StringClaim;
import it.smartcommunitylab.aac.common.InvalidDefinitionException;
import it.smartcommunitylab.aac.common.NoSuchServiceException;
import it.smartcommunitylab.aac.common.SystemException;
import it.smartcommunitylab.aac.core.ClientDetails;
import it.smartcommunitylab.aac.model.User;

public class ScriptServiceClaimExtractor implements ResourceClaimsExtractor {
    public static final String CLAIM_MAPPING_FUNCTION = "claimMapping";

    private final ObjectMapper mapper = new ObjectMapper();
    private final TypeReference<HashMap<String, Serializable>> serMapTypeRef = new TypeReference<HashMap<String, Serializable>>() {
    };
    private final TypeReference<ArrayList<Serializable>> serListTypeRef = new TypeReference<ArrayList<Serializable>>() {
    };
    private final ServicesService servicesService;
    private ScriptExecutionService executionService;

    // TODO add a loadingcache for scripts

    public ScriptServiceClaimExtractor(ServicesService servicesService) {
        Assert.notNull(servicesService, "services service is required");
        this.servicesService = servicesService;
    }

    public void setExecutionService(ScriptExecutionService executionService) {
        this.executionService = executionService;
    }

    @Override
    public Collection<String> getResourceIds() {
        // service namespace is resourceId
        return servicesService.listNamespaces();
    }

    @Override
    public ClaimsSet extractUserClaims(String resourceId, User user, ClientDetails client, Collection<String> scopes)
            throws InvalidDefinitionException, SystemException {

        if (executionService == null) {
            return null;
        }

        try {
            // fetch service
            Service se = servicesService.getServiceByNamespace(resourceId);
            if (se == null) {
                return null;
            }

            // fetch claimMapping
            String claimMapping = se.getUserClaimMapping();
            if (!StringUtils.hasText(claimMapping)) {
                return null;
            }

            // translate user, client and scopes to a map
            Map<String, Serializable> map = new HashMap<>();
            map.put("scopes", new ArrayList<>(scopes));
            map.put("user", mapper.convertValue(user, serMapTypeRef));
            map.put("client", mapper.convertValue(client, serMapTypeRef));

            // execute script
            Map<String, Serializable> customClaims = executionService.executeFunction(CLAIM_MAPPING_FUNCTION,
                    claimMapping,
                    map);

            // map to defined claims and build claimsSet
            List<Claim> claims = processClaims(se.getClaims(), customClaims);

            // build a claimsSet
            DefaultClaimsSet claimsSet = new DefaultClaimsSet();
            claimsSet.setResourceId(resourceId);
            claimsSet.setScope(null);
            claimsSet.setNamespace(resourceId);
            claimsSet.setUser(true);
            claimsSet.setClaims(claims);

            return claimsSet;

        } catch (NoSuchServiceException e) {
            return null;
        }
    }

    @Override
    public ClaimsSet extractClientClaims(String resourceId, ClientDetails client, Collection<String> scopes)
            throws InvalidDefinitionException, SystemException {
        if (executionService == null) {
            return null;
        }

        try {
            // fetch service
            Service se = servicesService.getServiceByNamespace(resourceId);
            if (se == null) {
                return null;
            }

            // fetch claimMapping
            String claimMapping = se.getClientClaimMapping();
            if (!StringUtils.hasText(claimMapping)) {
                return null;
            }

            // translate user, client and scopes to a map
            Map<String, Serializable> map = new HashMap<>();
            map.put("scopes", new ArrayList<>(scopes));
            map.put("client", mapper.convertValue(client, serMapTypeRef));

            // execute script
            Map<String, Serializable> customClaims = executionService.executeFunction(CLAIM_MAPPING_FUNCTION,
                    claimMapping,
                    map);

            // map to defined claims and build claimsSet
            List<Claim> claims = processClaims(se.getClaims(), customClaims);

            // build a claimsSet
            DefaultClaimsSet claimsSet = new DefaultClaimsSet();
            claimsSet.setResourceId(resourceId);
            claimsSet.setScope(null);
            claimsSet.setNamespace(resourceId);
            claimsSet.setUser(true);
            claimsSet.setClaims(claims);

            return claimsSet;

        } catch (NoSuchServiceException e) {
            return null;
        }
    }

    private List<Claim> processClaims(Collection<ServiceClaim> serviceClaimsList,
            Map<String, Serializable> customClaims) {
        List<Claim> claims = new ArrayList<>();
        Map<String, ServiceClaim> serviceClaims = serviceClaimsList.stream()
                .collect(Collectors.toMap(sc -> sc.getKey(), sc -> sc));

        for (Map.Entry<String, Serializable> entry : customClaims.entrySet()) {
            String key = entry.getKey();

            if (serviceClaims.containsKey(key)) {
                // try to parse as definition, or drop
                ServiceClaim model = serviceClaims.get(key);
                if (model.isMultiple()) {
                    // try to parse a collection
                    try {
                        ArrayList<Serializable> lis = mapper.convertValue(entry.getValue(), serListTypeRef);
                        if (lis == null) {
                            throw new IllegalArgumentException();
                        }

                        for (Serializable s : lis) {
                            Claim cm = parseClaim(model, s);
                            if (cm != null) {
                                claims.add(cm);
                            }
                        }

                    } catch (IllegalArgumentException ie) {
                        // single value
                        Claim cm = parseClaim(model, entry.getValue());
                        if (cm != null) {
                            claims.add(cm);
                        }
                    }
                } else {
                    // single value
                    Claim cm = parseClaim(model, entry.getValue());
                    if (cm != null) {
                        claims.add(cm);
                    }
                }

            }
        }

        return claims;
    }

    private Claim parseClaim(ServiceClaim model, Serializable value) {
        AbstractClaim c = null;
        try {
            switch (model.getType()) {
            case BOOLEAN:
                Boolean bvalue = mapper.convertValue(value, Boolean.class);
                c = new BooleanClaim(model.getKey(), bvalue);
                break;
            case NUMBER: {
                Number nvalue = mapper.convertValue(value, Number.class);
                c = new NumberClaim(model.getKey(), nvalue);
                break;
            }
            case STRING:
                String svalue = mapper.convertValue(value, String.class);
                c = new StringClaim(model.getKey(), svalue);
                break;
            case DATE:
                Date dvalue = mapper.convertValue(value, Date.class);
                c = new DateClaim(model.getKey(), dvalue);
                break;
            case OBJECT:
                HashMap<String, Serializable> mvalue = mapper.convertValue(value, serMapTypeRef);
                c = new SerializableClaim(model.getKey(), mvalue);
                break;
            default:

            }
        } catch (Exception e) {
            return null;
        }

        if (c != null) {
            c.setName(model.getName());
            c.setDescription(model.getDescription());
        }

        return c;
    }

}