package it.smartcommunitylab.aac.core.service;

import java.util.Collections;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import it.smartcommunitylab.aac.SystemKeys;
import it.smartcommunitylab.aac.common.NoSuchAuthorityException;
import it.smartcommunitylab.aac.common.RegistrationException;
import it.smartcommunitylab.aac.core.model.ConfigurableAttributeProvider;
import it.smartcommunitylab.aac.core.persistence.AttributeProviderEntity;
import it.smartcommunitylab.aac.core.provider.ConfigurationProvider;

@Service
@Transactional
public class AttributeProviderService
        extends ConfigurableProviderService<ConfigurableAttributeProvider, AttributeProviderEntity> {

    private AttributeProviderAuthorityService authorityService;

    public AttributeProviderService(AttributeProviderEntityService providerService) {
        super(providerService);
        setEntityConverter(new AttributeProviderEntityConverter());
        setConfigConverter(new AttributeProviderConfigConverter());
    }

    @Autowired
    public void setAuthorityService(AttributeProviderAuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    @Override
    protected ConfigurationProvider<?, ?, ?> getConfigurationProvider(String authority)
            throws NoSuchAuthorityException {
        return authorityService.getAuthority(authority).getConfigurationProvider();
    }

    class AttributeProviderEntityConverter
            implements Converter<AttributeProviderEntity, ConfigurableAttributeProvider> {

        @Override
        public ConfigurableAttributeProvider convert(AttributeProviderEntity pe) {
            ConfigurableAttributeProvider cp = new ConfigurableAttributeProvider(pe.getAuthority(), pe.getProviderId(),
                    pe.getRealm());
            cp.setConfiguration(pe.getConfigurationMap());
            cp.setEnabled(pe.isEnabled());
            cp.setPersistence(pe.getPersistence());
            cp.setEvents(pe.getEvents());
            cp.setName(pe.getName());
            cp.setDescription(pe.getDescription());

            Set<String> attributeSets = pe.getAttributeSets() != null
                    ? StringUtils.commaDelimitedListToSet(pe.getAttributeSets())
                    : Collections.emptySet();
            cp.setAttributeSets(attributeSets);

            return cp;
        }

    }

    class AttributeProviderConfigConverter
            implements Converter<ConfigurableAttributeProvider, AttributeProviderEntity> {

        @Override
        public AttributeProviderEntity convert(ConfigurableAttributeProvider reg) {
            AttributeProviderEntity pe = new AttributeProviderEntity();

            pe.setAuthority(reg.getAuthority());
            pe.setProviderId(reg.getProvider());
            pe.setRealm(reg.getRealm());
            pe.setEnabled(reg.isEnabled());

            String name = reg.getName();
            String description = reg.getDescription();
            if (StringUtils.hasText(name)) {
                name = Jsoup.clean(name, Safelist.none());
            }
            if (StringUtils.hasText(description)) {
                description = Jsoup.clean(description, Safelist.none());
            }

            pe.setName(name);
            pe.setDescription(description);

            // TODO add enum
            String persistence = reg.getPersistence();
            if (!StringUtils.hasText(persistence)) {
                persistence = SystemKeys.PERSISTENCE_LEVEL_REPOSITORY;
            }

            if (!SystemKeys.PERSISTENCE_LEVEL_REPOSITORY.equals(persistence)
                    && !SystemKeys.PERSISTENCE_LEVEL_MEMORY.equals(persistence)
                    && !SystemKeys.PERSISTENCE_LEVEL_SESSION.equals(persistence)
                    && !SystemKeys.PERSISTENCE_LEVEL_NONE.equals(persistence)) {
                throw new RegistrationException("invalid persistence level");
            }

            String events = reg.getEvents();
            if (!StringUtils.hasText(events)) {
                events = SystemKeys.EVENTS_LEVEL_DETAILS;
            }

            if (!SystemKeys.EVENTS_LEVEL_DETAILS.equals(events)
                    && !SystemKeys.EVENTS_LEVEL_FULL.equals(events)
                    && !SystemKeys.EVENTS_LEVEL_MINIMAL.equals(events)
                    && !SystemKeys.EVENTS_LEVEL_NONE.equals(events)) {
                throw new RegistrationException("invalid events level");
            }

            pe.setPersistence(persistence);
            pe.setEvents(events);

            pe.setConfigurationMap(reg.getConfiguration());

            pe.setAttributeSets(StringUtils.collectionToCommaDelimitedString(reg.getAttributeSets()));

            return pe;
        }

    }

}
