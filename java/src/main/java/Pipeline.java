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
        try {
            runTests(project);
            deployProject(project);
            sendEmail("Deployment completed successfully");
        } catch (TestsFailedException e) {
            sendEmail("Tests failed");
        } catch (DeployProjectFailedException e) {
            sendEmail("Deployment failed");
        }

    }

    private void runTests(Project project) throws TestsFailedException {
        if (!project.hasTests()) {
            log.info("No tests");
            return;
        }
        if (!"success".equals(project.runTests())) {
            log.error("Tests failed");
            throw new TestsFailedException();
        }

        log.info("Tests passed");
    }

    private static class TestsFailedException extends Exception {
        public TestsFailedException() {
        }
    }

    private void deployProject(Project project) throws DeployProjectFailedException {
        if (!"success".equals(project.deploy())) {
            log.error("Deployment failed");
            throw new DeployProjectFailedException();
        }
        log.info("Deployment successful");
    }

    private static class DeployProjectFailedException extends Throwable {
        public DeployProjectFailedException() {
        }
    }

    private void sendEmail(String message) {
        if (!config.sendEmailSummary()) {
            log.info("Email disabled");
            return;
        }
        log.info("Sending email");
        emailer.send(message);
    }
}
