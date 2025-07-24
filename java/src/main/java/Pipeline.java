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
        if (!project.hasTests()) {
            log.info("No tests");
            return true;
        }
        if ("success".equals(project.runTests())) {
            log.info("Tests passed");
            return true;
        }
        log.error("Tests failed");
        return false;
    }

    private boolean isDeploySuccessful(Project project) {
        if (!"success".equals(project.deploy())) {
            log.error("Deployment failed");
            return false;
        }
        log.info("Deployment successful");
        return true;
    }

    private void sendEmail(boolean testsPassed, boolean deploySuccessful) {
        if (config.sendEmailSummary()) {
            log.info("Sending email");
            String message = "Tests failed";
            if (testsPassed) {
                if (deploySuccessful) {
                    emailer.send("Deployment completed successfully");
                } else {
                    emailer.send("Deployment failed");
                }
            } else {
                emailer.send(message);
            }
        } else {
            log.info("Email disabled");
        }
    }
}
