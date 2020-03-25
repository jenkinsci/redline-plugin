package com.redline.jenkins.scenario;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.redline.jenkins.RedlineApi;
import com.redline.jenkins.RedlineBuilder;
import com.redline.jenkins.RedlineCredential;
import com.redline.jenkins.Thresholds;
import java.util.List;
import java.util.Map;

import jenkins.model.Jenkins;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Build task for executing a scenario.
 */
public class ScenarioBuilder extends RedlineBuilder {
    
    @DataBoundConstructor
    public ScenarioBuilder(
            String templateId,
            Thresholds thresholds
    ) {
        this.templateId = templateId;
        this.thresholds = thresholds;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder>{

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Redline13 Scenario";
        }

        /**
         * Display list of API Keys, using Jelly
         *
         * @return List of templates
         */
        public ListBoxModel doFillTemplateIdItems() {
            String apiKey = getApiKey();

            ListBoxModel items = new ListBoxModel();
            if (apiKey == null) {
                items.add("No API Key", "-1");
            } else {

                RedlineApi api = new RedlineApi(apiKey);

                try {

                    Map<String, String> testList = api.getTemplateList();
                    if (testList == null) {
                        items.add("Invalid API key ", "-1");
                    } else if (testList.isEmpty()) {
                        items.add("No tests", "-1");
                    } else {
                        for (Map.Entry<String, String> test : testList.entrySet()) {
                            items.add(test.getValue(), test.getKey());
                        }
                    }
                } catch (Exception e) {
                    items.add("Failed to find tests", "-1");
                }
            }

            return items;
        }

        public String getApiKey() {
            List<RedlineCredential> credentials = CredentialsProvider.lookupCredentials(RedlineCredential.class, Jenkins.getInstance(), ACL.SYSTEM);
            if (!credentials.isEmpty()) {
                return credentials.get(0).getApiKey().getPlainText();
            }
            // API key is not valid any more
            return null;
        }

    }

}
