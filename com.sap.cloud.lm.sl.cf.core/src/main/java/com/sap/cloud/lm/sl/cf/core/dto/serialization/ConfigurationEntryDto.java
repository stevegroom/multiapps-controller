package com.sap.cloud.lm.sl.cf.core.dto.serialization;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.sap.cloud.lm.sl.cf.core.model.CloudTarget;
import com.sap.cloud.lm.sl.cf.core.model.ConfigurationEntry;
import com.sap.cloud.lm.sl.cf.core.model.PersistenceMetadata;
import com.sap.cloud.lm.sl.cf.core.util.ConfigurationUtil;
import com.sap.cloud.lm.sl.mta.model.Version;

@XmlRootElement(name = "configuration-entry")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class ConfigurationEntryDto {
    @XmlElement
    private long id;

    @XmlElement(name = "provider-nid")
    private String providerNid;

    @XmlElement(name = "provider-id")
    private String providerId;

    @XmlElement(name = "provider-version")
    private String providerVersion;

    @XmlElement(name = "target-space")
    private String targetSpace;

    @XmlElement(name = "provider-target")
    private CloudTarget cloudTarget;

    @XmlElement
    private String content;

    @XmlElementWrapper(name = "visibility")
    @XmlElement(name = "target")
    private List<CloudTarget> visibility;

    public ConfigurationEntryDto() {
        // Required by JPA and JAXB.
    }

    public ConfigurationEntryDto(long id, String providerNid, String providerId, String providerVersion, CloudTarget cloudTarget,
        String content, List<CloudTarget> visibility) {
        this.id = id;
        this.providerNid = providerNid;
        this.providerId = providerId;
        this.providerVersion = providerVersion;
        this.cloudTarget = cloudTarget;
        this.content = content;
        this.visibility = visibility;
    }

    public ConfigurationEntryDto(ConfigurationEntry entry) {
        this.id = entry.getId();
        this.providerNid = getNotNull(entry.getProviderNid());
        this.providerId = entry.getProviderId();
        this.providerVersion = getNotNull(entry.getProviderVersion());
        this.cloudTarget = entry.getTargetSpace();
        this.content = entry.getContent();
        this.visibility = entry.getVisibility();
    }

    public long getId() {
        return id;
    }

    public String getProviderNid() {
        return providerNid;
    }

    public String getTargetSpace() {
        return targetSpace;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getProviderVersion() {
        return providerVersion;
    }

    public String getContent() {
        return content;
    }

    public CloudTarget getCloudTarget() {
        return cloudTarget;
    }

    public List<CloudTarget> getVisibility() {
        return visibility;
    }

    public ConfigurationEntry toConfigurationEntry() {
        CloudTarget target = cloudTarget == null ? ConfigurationUtil.createImplicitCloudTarget(targetSpace) : cloudTarget;
        return new ConfigurationEntry(id, getOriginal(providerNid), providerId, getParsedVersion(getOriginal(providerVersion)), target,
            content, visibility);
    }

    private Version getParsedVersion(String versionString) {
        if (versionString == null) {
            return null;
        }
        return Version.parseVersion(versionString);
    }

    private String getOriginal(String source) {
        if (source == null || PersistenceMetadata.NOT_AVAILABLE.equals(source)) {
            return null;
        }
        return source;
    }

    private String getNotNull(Object source) {
        if (source == null) {
            return PersistenceMetadata.NOT_AVAILABLE;
        }
        return source.toString();
    }
}
