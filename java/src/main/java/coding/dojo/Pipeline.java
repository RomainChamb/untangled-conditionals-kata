package coding.dojo;

import coding.dojo.dependencies.Config;
import coding.dojo.dependencies.Emailer;
import coding.dojo.dependencies.Logger;
import coding.dojo.dependencies.Project;

public class Pipeline {
    private final Config config;
    private final Emailer emailer;
    private final Logger log;

    public Pipeline(Config config, Emailer emailer, Logger log) {
        this.config = config;
        this.emailer = emailer;
        this.log = log;
    }

    public void run(Project project) {
        boolean testsPassed;
        boolean deploySuccessful;

        testsPassed = runTest(project);

        deploySuccessful = deploy(project, testsPassed);

        sendEmail(testsPassed, deploySuccessful);
    }

    private boolean runTest(Project project) {
        if (!project.hasTests()) {
            log.info("No tests");
            return true;
        }
        if ("failure".equals(project.runTests())) {
            log.error("Tests failed");
            return false;
        }
        log.info("Tests passed");
        return true;
    }

    private boolean deploy(Project project, boolean testsPassed) {
        if (!testsPassed) {
            return false;
        }
        if (!"success".equals(project.deploy())) {
            log.error("Deployment failed");
            return false;
        }
        log.info("Deployment successful");
        return true;
    }

    private void sendEmail(boolean testsPassed, boolean deploySuccessful) {
        if (!config.sendEmailSummary()) {
            log.info("Email disabled");
        } else {
            log.info("Sending email");
            String message = createMessageBody(testsPassed, deploySuccessful);
            emailer.send(message);
        }
    }

    private static String createMessageBody(boolean testsPassed, boolean deploySuccessful) {
        if (!testsPassed) {
            return "Tests failed";
        }
        if (!deploySuccessful) {
            return "Deployment failed";
        }
        return "Deployment completed successfully";
    }
}
