package copyleaks.sdk.api;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import copyleaks.sdk.api.helpers.FileHelpers;
import copyleaks.sdk.api.helpers.HttpURLConnection.CopyleaksClient;
import copyleaks.sdk.api.helpers.HttpURLConnection.HttpURLConnectionHelper;
import copyleaks.sdk.api.RequestMethod;
import copyleaks.sdk.api.exceptions.CommandFailedException;
import copyleaks.sdk.api.exceptions.SecurityTokenException;
import copyleaks.sdk.api.models.LoginToken;
import copyleaks.sdk.api.models.requests.CreateCommandRequest;
import copyleaks.sdk.api.models.responses.BadLoginResponse;
import copyleaks.sdk.api.models.responses.CreateResourceResponse;

/**
 * This class is the main gate for integration with Copyleaks servers.
 * 
 * With this class you can Scan a URL (for web-page or Internet file), or Upload
 * and scan of supported local file.
 *
 */
public class Detector
{
	/**
	 * Current user identity.
	 */
	private LoginToken Token;

	private LoginToken getToken()
	{
		return this.Token;
	}

	private void setToken(LoginToken token)
	{
		this.Token = token;
	}

	public Detector(LoginToken token)
	{
		this.setToken(token);
	}

	/**
	 * URL scan for copyrights infringement. This method start a scanning
	 * process on Copyleaks cloud.
	 * 
	 * @param url
	 *            Path for information that accessible via the Internet.
	 * @return A server process identification to track.
	 * @throws CommandFailedException
	 * @throws Exception
	 */
	public ScannerProcess CreateByUrl(URI url) throws SecurityTokenException, CommandFailedException
	{
		LoginToken.ValidateToken(this.getToken()); // Token Validation

		String json;
		Gson gson = new GsonBuilder().create();
		CreateCommandRequest model = new CreateCommandRequest();
		model.setURL(url.toString());
		URL reqUrl;
		HttpsURLConnection conn = null;
		try
		{
			reqUrl = new URL(Resources.ServiceEntryPoint + Resources.ServiceVersion + "/detector/create-by-url");
			conn = CopyleaksClient.getClient(reqUrl, this.getToken(), RequestMethod.POST, HttpContentTypes.Json,
					HttpContentTypes.Json);
			CopyleaksClient.HandleString.attach(conn, gson.toJson(model));

			if (conn.getResponseCode() != 200)
			{
				String errorResponse;
				try (InputStream inputStream = new BufferedInputStream(conn.getErrorStream()))
				{
					errorResponse = HttpURLConnectionHelper.convertStreamToString(inputStream);
				}
				BadLoginResponse response = gson.fromJson(errorResponse, BadLoginResponse.class);
				if (response == null)
					throw new RuntimeException("Unable to process server response.");
				else
					throw new CommandFailedException(response.getMessage(), conn.getResponseCode());
			}

			try (InputStream inputStream = new BufferedInputStream(conn.getInputStream()))
			{
				json = HttpURLConnectionHelper.convertStreamToString(inputStream);
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e.getMessage());
		}
		finally
		{
			if (conn != null)
				conn.disconnect();
		}

		if (json == null || json.isEmpty())
			throw new RuntimeException("Unable to process server response.");

		CreateResourceResponse response = gson.fromJson(json, CreateResourceResponse.class);
		return new ScannerProcess(this.getToken(), response.getProcessId());
	}

	/**
	 * Local file scan for copyrights infringement. This method start a scanning
	 * process on Copyleaks cloud.
	 * 
	 * @param localfile
	 *            Path for local file. This file must a textual file that it's
	 *            format supported by Copyleaks. This file will be uploaded to
	 *            Copyleaks server and will be remove at the end of the
	 *            processing stage.
	 * @return A server process identification to track.
	 * @throws CommandFailedException
	 * @throws Exception
	 */
	public ScannerProcess CreateByFile(File localfile) throws SecurityTokenException, CommandFailedException
	{
		LoginToken.ValidateToken(this.getToken()); // Token Validation

		String json;
		Gson gson = new GsonBuilder().create();
		URL reqUrl;
		HttpsURLConnection conn = null;
		try
		{
			reqUrl = new URL(Resources.ServiceEntryPoint + Resources.ServiceVersion + "/detector/create-by-file");
			conn = CopyleaksClient.getClient(reqUrl, this.getToken(), RequestMethod.POST,
					HttpContentTypes.Multipart + ";boundary=file." + FileHelpers.getFileExtension(localfile),
					HttpContentTypes.Json);
			CopyleaksClient.HandleFile.attach(conn, localfile);

			if (conn.getResponseCode() != 200)
			{
				String errorResponse;
				try (InputStream inputStream = new BufferedInputStream(conn.getErrorStream()))
				{
					errorResponse = HttpURLConnectionHelper.convertStreamToString(inputStream);
				}
				BadLoginResponse response = gson.fromJson(errorResponse, BadLoginResponse.class);
				if (response == null)
					throw new RuntimeException("Unable to process server response.");
				else
					throw new CommandFailedException(response.getMessage(), conn.getResponseCode());
			}

			try (InputStream inputStream = new BufferedInputStream(conn.getInputStream()))
			{
				json = HttpURLConnectionHelper.convertStreamToString(inputStream);
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e.getMessage());
		}
		finally
		{
			if (conn != null)
				conn.disconnect();
		}

		if (json == null || json.isEmpty())
			throw new RuntimeException("Unable to process server response.");

		CreateResourceResponse response = gson.fromJson(json, CreateResourceResponse.class);
		return new ScannerProcess(this.getToken(), response.getProcessId());
	}

	/**
	 * Image scan, with OCR, for copyrights infringement. This method start a
	 * scanning process on Copyleaks cloud.
	 * 
	 * @param localfile
	 *            Path for local image file. Expected image formats: jpg, jpeg.
	 *            This file will be uploaded to Copyleaks server and will be
	 *            remove at the end of the processing stage.
	 * @return A server process identification to track.
	 * @throws CommandFailedException 
	 * @throws Exception
	 */
	public ScannerProcess CreateByOCR(File localfile) 
			throws CommandFailedException 
	{
		LoginToken.ValidateToken(this.getToken()); // Token Validation

		String json;
		Gson gson = new GsonBuilder().create();
		URL reqUrl;
		HttpsURLConnection conn = null;
		try
		{
			reqUrl = new URL(Resources.ServiceEntryPoint + Resources.ServiceVersion + "/detector/create-by-file-ocr");
			conn = CopyleaksClient.getClient(reqUrl, this.getToken(), RequestMethod.POST,
					HttpContentTypes.Multipart + ";boundary=file." + FileHelpers.getFileExtension(localfile),
					HttpContentTypes.Json);
			CopyleaksClient.HandleFile.attach(conn, localfile);

			if (conn.getResponseCode() != 200)
			{
				String errorResponse;
				try (InputStream inputStream = new BufferedInputStream(conn.getErrorStream()))
				{
					errorResponse = HttpURLConnectionHelper.convertStreamToString(inputStream);
				}
				BadLoginResponse response = gson.fromJson(errorResponse, BadLoginResponse.class);
				if (response == null)
					throw new RuntimeException("Unable to process server response.");
				else
					throw new CommandFailedException(response.getMessage(), conn.getResponseCode());
			}

			try (InputStream inputStream = new BufferedInputStream(conn.getInputStream()))
			{
				json = HttpURLConnectionHelper.convertStreamToString(inputStream);
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e.getMessage());
		}
		finally
		{
			if (conn != null)
				conn.disconnect();
		}

		if (json == null || json.isEmpty())
			throw new RuntimeException("Unable to process server response.");

		CreateResourceResponse response = gson.fromJson(json, CreateResourceResponse.class);
		return new ScannerProcess(this.getToken(), response.getProcessId());
		// // Token Validation
		// if (this.getToken() == null)
		// throw new Exception("Empty token!");
		// else
		// this.getToken().Validate();
		// // File Validation
		// if (localfile == null)
		// throw new FileNotFoundException();
		//
		// CreateCommandRequest req = new CreateCommandRequest();
		// req.setURL(localfile.getFile().getPath());
		// HttpClient client = HttpClientBuilder.create().build();
		// Gson gson = new GsonBuilder().create();
		// MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		// // Post to the server
		// HttpPost post = new HttpPost(Resources.ServiceEntryPoint +
		// Resources.ServiceVersion + "/detector/create-by-file-ocr");
		// builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		// builder.addPart("document", localfile);
		// //post.addHeader("Accept", Content_Type.Json);
		// post.setHeader("User-Agent", Resources.USER_AGENT);
		// post.addHeader("Authorization", String.format("Bearer %1$s",
		// this.getToken().getTemporarySecurityCode()));
		// HttpEntity entity = builder.build();
		// post.setEntity(entity);
		// // Response from the server
		// HttpResponse msg = client.execute(post);
		// HttpEntity msgEntity = msg.getEntity();
		// if (msg.getStatusLine().getStatusCode() != 200) {
		// String json_error = EntityUtils.toString(entity, "UTF-8");
		// BadResponse error = gson.fromJson(json_error, BadResponse.class);
		// if (json_error == null)
		// throw new CommandFailedException(msg);
		// else
		// throw new CommandFailedException(error.Message, msg);
		// }
		//
		// String json = EntityUtils.toString(msgEntity, "UTF-8");
		// CreateResourceResponse response = gson.fromJson(json,
		// CreateResourceResponse.class);
		// return new ScannerProcess(this.getToken(), response.getProcessId());
	}
}
