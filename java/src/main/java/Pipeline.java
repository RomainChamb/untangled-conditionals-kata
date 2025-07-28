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
        if(!runTests(project)) {
            sendEmail("Tests failed");
            return;
        }
        if (!deployProject(project)) {
            sendEmail("Deployment failed");
            return;
        }
        sendEmail("Deployment completed successfully");
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

    private boolean deployProject(Project project) {
        if (!"success".equals(project.deploy())) {
            log.error("Deployment failed");
            return false;
        }
        log.info("Deployment successful");
        return true;
    }

    private void sendEmail(String message) {
        if (config.sendEmailSummary()) {
            log.info("Sending email");
            emailer.send(message);
        } else {
            log.info("Email disabled");
        }
    }
}
