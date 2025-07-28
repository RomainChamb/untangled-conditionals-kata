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

        testsPassed = runTests(project);
        deploySuccessful = deployProject(project, testsPassed);

        sendEmail(testsPassed, deploySuccessful);
    }

    private boolean deployProject(Project project, boolean testsPassed) {
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

    private boolean runTests(Project project) {
        if (!project.hasTests()) {
            log.info("No tests");
            return true;
        }
        if (!"success".equals(project.runTests())) {
            log.error("Tests failed");
            return false;
        }

        log.info("Tests passed");
        return true;
    }

    private void sendEmail(boolean testsPassed, boolean deploySuccessful) {
        if (config.sendEmailSummary()) {
            log.info("Sending email");
            String message = "";
            if (testsPassed) {
                if (deploySuccessful) {
                    emailer.send("Deployment completed successfully");
                } else {
                    emailer.send("Deployment failed");
                }
            } else {
                message = "Tests failed";
                emailer.send(message);
            }
        } else {
            log.info("Email disabled");
        }
    }
}
