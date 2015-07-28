package coolerteam.com.omco.ci.jenkins.web.builder;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

import net.sf.json.JSONObject;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class WebBuilder extends Builder implements Serializable {

	public final String group;
	public final String warFilePath;

	@DataBoundConstructor
	public WebBuilder(String warFilePath, String group) {
		this.group = group;
		this.warFilePath = warFilePath;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {
		EnvVars env = build.getEnvironment();
		MultipartEntityBuilder mutipartEntityBuilder = MultipartEntityBuilder
				.create();
		mutipartEntityBuilder.addBinaryBody("files[]",
				new File(env.expand(this.warFilePath)));
		mutipartEntityBuilder.addTextBody("group", this.group);
		CloseableHttpResponse response = null;
		HttpPost post = new HttpPost(getDescriptor().getDistributionServer());
		post.setEntity(mutipartEntityBuilder.build());
		CloseableHttpClient httpClient = HttpClients.createDefault();
		response = httpClient.execute(post);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				response.getEntity().getContent()));
		String line = null;
		while ((line = reader.readLine()) != null) {
			listener.getLogger().println(line);
		}
		return true;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		// TODO Auto-generated method stub
		return (DescriptorImpl) super.getDescriptor();
	}

	@Extension
	public static final class DescriptorImpl extends
			BuildStepDescriptor<Builder> {

		private String distributionServer;

		public String getDistributionServer() {
			return distributionServer;
		}

		public void setDistributionServer(String distributionServer) {
			this.distributionServer = distributionServer;
		}

		public DescriptorImpl() {
			load();
		}

		public boolean configure(StaplerRequest req, JSONObject json)
				throws Descriptor.FormException {
			this.distributionServer = json.getString("distributionServer");
			save();
			return super.configure(req, json);
		}

		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}

		public String getDisplayName() {
			return "Web自动部署";
		}

	}
}
