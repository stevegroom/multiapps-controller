package com.sap.cloud.lm.sl.cf.process.steps;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.cf.detect.ApplicationMtaMetadataParser;
import com.sap.cloud.lm.sl.cf.core.dao.ConfigurationEntryDao;
import com.sap.cloud.lm.sl.cf.core.model.ApplicationMtaMetadata;
import com.sap.cloud.lm.sl.cf.core.model.CloudTarget;
import com.sap.cloud.lm.sl.cf.core.model.ConfigurationEntry;
import com.sap.cloud.lm.sl.cf.core.util.ConfigurationEntriesUtil;
import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.message.Messages;
import com.sap.cloud.lm.sl.common.NotFoundException;
import com.sap.cloud.lm.sl.common.util.JsonUtil;

@Component("deleteDiscontinuedConfigurationEntriesForAppStep")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeleteDiscontinuedConfigurationEntriesForAppStep extends SyncFlowableStep {

    @Inject
    private ConfigurationEntryDao configurationEntryDao;

    @Override
    protected StepPhase executeStep(ExecutionWrapper execution) {
        CloudApplication existingApp = StepsUtil.getExistingApp(execution.getContext());
        if (existingApp == null) {
            return StepPhase.DONE;
        }
        getStepLogger().info(Messages.DELETING_DISCONTINUED_CONFIGURATION_ENTRIES_FOR_APP, existingApp.getName());
        String mtaId = (String) execution.getContext()
            .getVariable(Constants.PARAM_MTA_ID);
        ApplicationMtaMetadata mtaMetadata = ApplicationMtaMetadataParser.parseAppMetadata(existingApp);
        if (mtaMetadata == null) {
            return StepPhase.DONE;
        }
        List<String> providedDependencyNames = mtaMetadata.getProvidedDependencyNames();
        String org = StepsUtil.getOrg(execution.getContext());
        String space = StepsUtil.getSpace(execution.getContext());
        CloudTarget target = new CloudTarget(org, space);
        String oldMtaVersion = mtaMetadata.getMtaMetadata()
            .getVersion()
            .toString();
        List<ConfigurationEntry> publishedEntries = StepsUtil.getPublishedEntries(execution.getContext());

        List<ConfigurationEntry> entriesToDelete = getEntriesToDelete(mtaId, oldMtaVersion, target, providedDependencyNames,
            publishedEntries);
        for (ConfigurationEntry entry : entriesToDelete) {
            try {
                configurationEntryDao.remove(entry.getId());
            } catch (NotFoundException e) {
                getStepLogger().warn(Messages.COULD_NOT_DELETE_PROVIDED_DEPENDENCY, entry.getProviderId());
            }
        }
        getStepLogger().debug(Messages.DELETED_ENTRIES, JsonUtil.toJson(entriesToDelete, true));
        StepsUtil.setDeletedEntries(execution.getContext(), entriesToDelete);

        getStepLogger().debug(Messages.DISCONTINUED_CONFIGURATION_ENTRIES_FOR_APP_DELETED, existingApp.getName());
        return StepPhase.DONE;
    }

    private List<ConfigurationEntry> getEntriesToDelete(String mtaId, String mtaVersion, CloudTarget target,
        List<String> providedDependencyNames, List<ConfigurationEntry> publishedEntries) {
        List<ConfigurationEntry> entriesForCurrentMta = getEntries(mtaId, mtaVersion, target);
        List<ConfigurationEntry> entriesForCurrentModule = getConfigurationEntriesWithProviderIds(entriesForCurrentMta,
            getProviderIds(mtaId, providedDependencyNames));
        return getEntriesNotUpdatedByThisProcess(entriesForCurrentModule, publishedEntries);
    }

    private List<ConfigurationEntry> getEntriesNotUpdatedByThisProcess(List<ConfigurationEntry> entriesForCurrentModule,
        List<ConfigurationEntry> publishedEntries) {
        return entriesForCurrentModule.stream()
            .filter(entry -> !hasId(entry, publishedEntries))
            .collect(Collectors.toList());
    }

    private boolean hasId(ConfigurationEntry entry, List<ConfigurationEntry> publishedEntries) {
        return publishedEntries.stream()
            .anyMatch(publishedEntry -> publishedEntry.getId() == entry.getId());
    }

    private List<String> getProviderIds(String mtaId, List<String> providedDependencyNames) {
        return providedDependencyNames.stream()
            .map(providedDependencyName -> ConfigurationEntriesUtil.computeProviderId(mtaId, providedDependencyName))
            .collect(Collectors.toList());
    }

    private List<ConfigurationEntry> getConfigurationEntriesWithProviderIds(List<ConfigurationEntry> entries, List<String> providerIds) {
        return entries.stream()
            .filter(entry -> hasProviderId(entry, providerIds))
            .collect(Collectors.toList());
    }

    private boolean hasProviderId(ConfigurationEntry entry, List<String> providerIds) {
        return providerIds.stream()
            .anyMatch(providerId -> entry.getProviderId()
                .equals(providerId));
    }

    private List<ConfigurationEntry> getEntries(String mtaId, String mtaVersion, CloudTarget target) {
        return configurationEntryDao.find(ConfigurationEntriesUtil.PROVIDER_NID, null, mtaVersion, target, null, mtaId);
    }

}
