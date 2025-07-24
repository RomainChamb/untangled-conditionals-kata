import dependencies.Config;
import dependencies.Emailer;
import dependencies.Logger;
import dependencies.Project;

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

        testsPassed = hasTestsPassed(project);

        if (testsPassed) {
            deploySuccessful = isDeploySuccessful(project);
        } else {
            deploySuccessful = false;
        }

        sendEmail(testsPassed, deploySuccessful);
    }

    private boolean hasTestsPassed(Project project) {
        boolean testsPassed;
        if (project.hasTests()) {
            if ("success".equals(project.runTests())) {
                log.info("Tests passed");
                testsPassed = true;
            } else {
                log.error("Tests failed");
                testsPassed = false;
            }
        } else {
            log.info("No tests");
            testsPassed = true;
        }
        return testsPassed;
    }

    private boolean isDeploySuccessful(Project project) {
        boolean deploySuccessful;
        if ("success".equals(project.deploy())) {
            log.info("Deployment successful");
            deploySuccessful = true;
        } else {
            log.error("Deployment failed");
            deploySuccessful = false;
        }
        return deploySuccessful;
    }

    private void sendEmail(boolean testsPassed, boolean deploySuccessful) {
        if (config.sendEmailSummary()) {
            log.info("Sending email");
            if (testsPassed) {
                if (deploySuccessful) {
                    emailer.send("Deployment completed successfully");
                } else {
                    emailer.send("Deployment failed");
                }
            } else {
                emailer.send("Tests failed");
            }
        } else {
            log.info("Email disabled");
        }
    }
}
